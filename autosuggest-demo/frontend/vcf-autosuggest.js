/*
 * Copyright 2000-2021 Vaadin Ltd.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */

import { LitElement, html, css } from 'lit-element';
import '@vaadin/vaadin-text-field';
import '@vaadin/vaadin-list-box';
import '@vaadin/vaadin-item';
import '@vaadin/vaadin-overlay';
import './vcf-autosuggest-overlay';

/**
 * `<vcf-autosuggest>` Web Component with a text input that provides a panel of suggested options.
 * Provides features such as advanced customization, lazy loading and label generator.
 *
 * ```html
 * <vcf-autosuggest></vcf-autosuggest>
 * ```
 *
 * @memberof Vaadin
 * @mixes ElementMixin
 * @mixes ThemableMixin
 * @demo demo/index.html
 */
class VcfAutosuggest extends LitElement {

    static get is() {
        return 'vcf-autosuggest';
    }

    static get properties() {
        return {
            limit: { type: Number },
            lazy: { type: Boolean },
            opened: { type: Boolean },
            loading: { type: Boolean },
            readOnly: { type: Boolean },
            caseSensitive: { type: Boolean },
            openDropdownOnClick: { type: Boolean },
            disableSearchHighlighting: { type: Boolean},
            minimumInputLengthToPerformLazyQuery: { type: Boolean },
            searchMatchingMode: { type: String },
            customItemTemplate: { type: String },
            inputValue: { type: String },
            options: { type: Array },
            optionsForWhenValueIsNull: { type: Array },
            defaultOption: { type: Object },

            _showNoResultsItem: { type: Boolean },
            _showInputLengthBelowMinimumItem: { type: Boolean },
            _noResultsMsg: { type: String },
            _inputLengthBelowMinimumMsg: { type: String },
            _optionsToDisplay: { type: Array },
            _boundOutsideClickHandler: { type: Object },
            _boundSetOverlayPosition: { type: Object }
        };
    }

    static get styles() {
        return css`
            :host .container {
                padding: 2px;
            }

            :host {
                display: inline-block;
            }
        `;
    }

    render() {
         return html`
            <div class="container">
                <vaadin-text-field id="textField" @focus="${this._textFieldFocused}"></vaadin-text-field>
                <vcf-autosuggest-overlay id="autosuggestOverlay">
                    <vaadin-list-box id="optionsContainer" part="options-container" style="margin: 0;">
                        ${this.loading
                            ? this.renderLoading()
                            : (this._showNoResultsItem
                                ? this.renderNoResults()
                                : (this._showInputLengthBelowMinimumItem
                                    ? this.renderInputLengthBelowMinimum()
                                    : this.renderOptions()))}
                    </vaadin-list-box>
                    <div id="dropdownEndSlot" part="dropdown-end-slot" style="padding-left: 0.5em; padding-right: 0.5em;">
                    </div>
                </vcf-autosuggest-overlay>
            </div>
         `;
    }

    renderLoading() {
        return html`
            <vaadin-item disabled part="option" class="loading">
                <div part="loading-indicator"></div>
            </vaadin-item>
        `;
    }

    renderNoResults() {
        return html`
            <vaadin-item disabled part="option" class="no-results">
                <div part="no-results"></div>
            </vaadin-item>
        `;
    }

    renderInputLengthBelowMinimum() {
        return html`
            <vaadin-item disabled part="option" class="input-length-below-minimum">
                <div part="input-length-below-minimum"></div>
            </vaadin-item>
        `;
    }

    renderOptions() {
        return html`
            ${this._optionsToDisplay.map( (option) =>
                typeof this.customItemTemplate === 'undefined' || this.customItemTemplate == null
                ? html`
                <vaadin-item @click="${this._optionClicked}" part="option" data-oid="${option.optId}" data-key="${option.key}">
                    ${this._getSuggestedStart(this.inputValue, option)}<span part="bold">${this._getInputtedPart(this.inputValue, option)}</span>${this._getSuggestedEnd(this.inputValue, option)}
                </vaadin-item>
                `
                : html`
                <vaadin-item @click="${this._optionClicked}" part="option" data-oid="${option.optId}" data-key="${option.key}" data-tag="autosuggestOverlayItem"></vaadin-item>
                `
            )}
        `;
    }

    constructor() {
        super();
        this.inputValue = '';
        this.options = [];
        this._optionsToDisplay = [];
        this.optionsForWhenValueIsNull = [];
        this.searchMatchingMode = 'STARTS_WITH';
        this._boundSetOverlayPosition = this._setOverlayPosition.bind(this);
        this._boundOutsideClickHandler = this._outsideClickHandler.bind(this);
    }

    connectedCallback() {
        super.connectedCallback();
        document.addEventListener('click', this._boundOutsideClickHandler);
    }

    firstUpdated() {
        this._textField = this.shadowRoot.getElementById('textField');
        this._textField.addEventListener('input', this._onInput.bind(this));
        this.addEventListener('iron-resize', this._boundSetOverlayPosition);
        this.addEventListener('click', this._elementClickListener);
        this.addEventListener('blur', this._elementBlurListener);
        this.addEventListener('keydown', this._onKeyDown.bind(this));
        this._overlayElement = this.shadowRoot.getElementById('autosuggestOverlay');
        this._overlayElement.addEventListener('vaadin-overlay-outside-click', ev => { this._cancelEvent(ev); });
        this._optionsContainer = this.shadowRoot.getElementById('optionsContainer');
        this._dropdownEndSlot = this.shadowRoot.getElementById('dropdownEndSlot');
        this._dropdownEndSlot.addEventListener('click', ev => { this._cancelEvent(ev); });
        this._defaultOptionChanged(this.defaultOption);
        if (this._noResultsMsg) this.setNoResultsMessage(this._noResultsMsg);
        if (this._inputLengthBelowMinimumMsg) this.setInputLengthBelowMinimumMessage(this._inputLengthBelowMinimumMsg);
    }

    disconnectedCallback() {
        super.disconnectedCallback();
        document.removeEventListener('click', this._boundOutsideClickHandler);
    }

    _textFieldFocused() {
        if (this.inputValue && this.inputValue.length > 0) this._openOverlay();
    }

    updated(changedProperties) {
        changedProperties.forEach((oldValue, propName) => {
            const newValue = JSON.stringify(this[propName]);
            console.log(`${propName} changed. oldValue: ${oldValue} newValue: ${newValue}`);
            if (propName == "opened") {
                this._openedChange(newValue === 'true');
            } else if (propName == 'options') {
                this._refreshOptionsToDisplay(this.options, this.input);
			} else if (propName == 'defaultOption') {
				this._defaultOptionChanged(this.defaultOption);
            } else if (propName == '_optionsToDisplay') {
                this._optionsToDisplayChanged(this._optionsToDisplay, this.opened);
            }
        });
        return true;
    }

    _elementClickListener(event) {
        if (this.openDropdownOnClick) this._openOverlay();
        event.stopPropagation();
    }

    _outsideClickHandler() {
        if (!this.opened || this === document.activeElement) return;
        this._applyValue(this.selectedValue == null ? (this._hasDefaultOption() ? this.defaultOption.key : '') : this.selectedValue);
        this.opened = false;
    }

    _onInput(event) {
        if (event.target != null && event.target.value != null) {
            this.inputValue = event.target.value.trim();
        } else {
            this.inputValue = '';
        }
        this._refreshOptionsToDisplay(this.options, this.inputValue)
        if (this.lazy && this.inputValue.length >= this.minimumInputLengthToPerformLazyQuery) this.loading = true;
        if (this.inputValue.length > 0) this._openOverlay();
        this._refreshMessageItemsState();
    }

    _openOverlay() {
        if (!this.readOnly && !this.opened) this.opened = true;
    }

    _openedChange(opened) {
        this._overlayElement.opened = opened
        if (opened) {
            this._setOverlayPosition();
            this._refreshOptionsToDisplay(this.options, this.inputValue);
            window.addEventListener('scroll', this._boundSetOverlayPosition, true);
            this._textField.addEventListener('wheel', this._cancelEvent, true);
        } else {
            window.removeEventListener('scroll', this._boundSetOverlayPosition, true);
            this._textField.removeEventListener('wheel', this._cancelEvent, true);
        }
    }

    _setOverlayPosition() {
        const inputRect = this._textField.getBoundingClientRect();
        if (this._overlayElement != null) {
            this._overlayElement.style.left = inputRect.left + 'px';
            this._overlayElement.style.top = inputRect.bottom + window.pageYOffset + 'px';
            this._overlayElement.updateStyles({ '--vcf-autosuggest-options-width': inputRect.width + 'px' });
        }
    }

    setNoResultsMessage(msg) {
        if (this._overlayElement != null) {
            this._overlayElement.updateStyles({ '--x-no-results-msg': '\'' + msg + '\'' });
            this._noResultsMsg = null;
        } else {
            this._noResultsMsg = msg;
        }
    }

    setInputLengthBelowMinimumMessage(msg) {
        if (this._overlayElement != null) {
            this._overlayElement.updateStyles({ '--x-input-length-below-minimum-msg': '\'' + msg + '\'' });
            this._inputLengthBelowMinimumMsg = null;
        } else {
            this._inputLengthBelowMinimumMsg = msg;
        }
    }

    _loadingChanged(v) {
        this.loading = !v
        this.loading = v //FORCE RE-RENDER
        this._refreshMessageItemsState();
    }

    _defaultOptionChanged(o) {
        if (o != null) {
            this.defaultOption = o;
            this._textField.value = o.label;
            this._savedValue = null
            this.opened = false;
            this._textFieldFocus(false);
        }
    }

    _optionClicked(ev) {
        this._applyValue(ev.target.dataset.key);
    }

    _applyValue(value, keepDropdownOpened=false) {
        if (value == null && this._hasDefaultOption()) value = this.defaultOption.key;
        this.selectedValue = (this._hasDefaultOption() && value == this.defaultOption.key ? null : value);

        let optLbl = "";
        let opt = this.options.find(x => x.key == value)
        if (!opt) opt = this.optionsForWhenValueIsNull.find(x => x.key == value)
        if (!opt) opt = this._hasDefaultOption() ? this.defaultOption : null
        optLbl = opt!=null ? opt.label : '';
        this.dispatchEvent(
            new CustomEvent('vcf-autosuggest-value-applied', {
                bubbles: true,
                detail: {
                    label: optLbl,
                    value: value
                }
            })
        );
        this._changeTextFieldValue(optLbl);
        if (!keepDropdownOpened) {
            this.opened = false;
            this._textFieldFocus(false)
        }
    }

    _textFieldFocus(focus=true) {
        if (focus)
            this._textField.focus();
        else
            this._textField.blur();
    }

    clear(keepDropdownOpened=false) {
        if (!keepDropdownOpened) this._applyValue(this._hasDefaultOption() ? this.defaultOption.key : '', true);
        this._textFieldFocus();
        if (!keepDropdownOpened) {
            this.opened = false;
            this._textFieldFocus(false);
        }
    }

    _changeTextFieldValue(newValue) {
            this._textField.value = newValue;

            this._textField.dispatchEvent(
                new Event('input', {
                    bubbles: true,
                    cancelable: true
                })
            );

            this._textField.dispatchEvent(
                new Event('value-changed', {
                    bubbles: true,
                    cancelable: true
                })
            );

            this._textField.dispatchEvent(
                new Event('change', {
                    bubbles: true,
                    cancelable: true
                })
            );

        this._inputValueChanged(newValue);
    }

    _inputValueChanged(value) {
        if (this._selectedOption) {
            this._selectedOption._setFocused(false);
            this._selectedOption = null;
        }
        if (value.length > 0 && !this.opened) this._openOverlay();
        else if (value.length == 0 && this.opened && !this.openDropdownOnClick) this.opened = false;
        this.dispatchEvent(
            new CustomEvent('vcf-autosuggest-input-value-changed', {
                bubbles: true,
                detail: {
                    value: value
                }
            })
        );
    }

    _refreshOptionsToDisplay(options, value) {
        if (typeof value === 'undefined') value = null;
        let _res = [];
        if (this._customizeOptionsForWhenValueIsNull() && (value == null || value.length == 0 || value.trim() == (this._hasDefaultOption() ? this.defaultOption.label : '').trim()))
            _res = _res.concat(this._limitOptions(this.optionsForWhenValueIsNull));
        else _res = _res.concat(this._limitOptions(this._filterOptions(options, value)));
        if (!_res || _res==null) _res = [];

        // Criteria for showing the default option:
        // 1. The input value is "" and the default value's key is not present in the optionsForWhenValueIsNull list
        // 2. It matches with the default option and the key is not in the results already
        if (this._hasDefaultOption()) {
            if (value == null || value.length == 0) {
                if (!this._customizeOptionsForWhenValueIsNull()) _res.unshift({label: this.defaultOption.label, searchStr: this.defaultOption.searchStr, key: this.defaultOption.key});
                else if (_res.filter(opt => opt.key == this.defaultOption.key).length == 0) _res.unshift({label: this.defaultOption.label, searchStr: this.defaultOption.searchStr, key: this.defaultOption.key});
            } else if (value.length > 0 && this._filterOptions([this.defaultOption], value).length > 0 && _res.filter(opt => opt.key == this.defaultOption.key).length == 0)
                _res.unshift({label: this.defaultOption.label, searchStr: this.defaultOption.searchStr, key: this.defaultOption.key});
        }

        for(let i=0; i<_res.length; i++) { _res[i].optId = i; }
        this._optionsToDisplay = _res;
        this._loadingChanged(false);
    }

    _hasDefaultOption() {
        return (this.defaultOption != null && this.defaultOption.key != null);
    }

    _limitOptions(options) {
        if (!options) return [];
        if (this.limit != null) return options.slice(0, this.limit);
        else return options;
    }

    _filterOptions(opts, v) {
        if (v == null || v.trim().length == 0 || v.trim() == (this._hasDefaultOption() ? this.defaultOption.label : '').trim()) return opts;
        let res = opts.filter(opt => {
            switch(this.searchMatchingMode) {
                case "CONTAINS":
                    return this.caseSensitive ? opt.searchStr.trim().includes(v.trim()) : opt.searchStr.trim().toLowerCase().includes(v.toLowerCase());
                case "STARTS_WITH":
                    return this.caseSensitive ? opt.searchStr.trim().startsWith(v.trim()) : opt.searchStr.trim().toLowerCase().startsWith(v.trim().toLowerCase());
                default:
                    return false;
            }
        });
        return res;
    }

    _refreshMessageItemsState() {
        if (!(this.lazy && this.minimumInputLengthToPerformLazyQuery > 0)) {
            this._showNoResultsItem = this._optionsToDisplay.length == 0 && !this.loading;
            this._showInputLengthBelowMinimumItem = false;
        } else {
            this._showNoResultsItem = this._optionsToDisplay.length == 0 && !this.loading && this.inputValue.length >= this.minimumInputLengthToPerformLazyQuery;
            if (!this._showNoResultsItem && !this.loading && this._optionsToDisplay.length == 0){
                this._showInputLengthBelowMinimumItem = true;
            } else {
                this._showInputLengthBelowMinimumItem = false;
            }
        }
    }

    _getSuggestedStart(value, option) {
        if (this.disableSearchHighlighting || !option.label || !value) return;
        if (option.label.trim().length == 0) return;
        return option.label.substr(0, this._getValueIndex(value, option));
    }

    _getInputtedPart(value, option) {
        if (this.disableSearchHighlighting || !option || !option.label || !value) return;
        if (option.label.trim().length == 0) return;
        if (!value) return option.label;
        return option.label.substr(this._getValueIndex(value, option), value.length);
    }

    _getSuggestedEnd(value, option) {
        if (this.disableSearchHighlighting) return option.label;
        if (!option.label) return;
        if (option.label && option.label.trim().length == 0) return;
        return option.label.substr(this._getValueIndex(value, option) + value.length, option.searchStr.length);
    }

    _getValueIndex(value, option) {
        return option.label.toLowerCase().indexOf(value.toLowerCase()) >= 0 ? option.label.toLowerCase().indexOf(value.toLowerCase()) : 0;
    }

    _customizeOptionsForWhenValueIsNull() {
        return this.optionsForWhenValueIsNull != null && this.optionsForWhenValueIsNull.length > 0;
    }

    _cancelEvent(ev) {
        ev.preventDefault();
        ev.stopPropagation();
    }

    _navigate(to) {
        const items = this._optionsContainer.items.filter(item => !item.disabled);
        if (!items.length) return;
        const index = items.indexOf(this._selectedOption);
        // Store the current value if an arrow clicked in the first time
        if (index === -1) this._savedValue = this.inputValue;
        // Reset the previously selected option
        if (this._selectedOption) {
            this._selectedOption._setFocused(false);
            this._selectedOption = null;
        }
        let nextIndex;
        // Calculate where to navigate next
        if (to === 'next') {
            nextIndex = index + 1;
            // If out of bounds then navigate to -1, which means 'previously stored value'
            if (nextIndex > items.length - 1) nextIndex = 0;
        } else if (to === 'prev') {
            nextIndex = index - 1;
            // If out of bounds then navigate to -1, which means 'previously stored value'
            if (nextIndex < 0) nextIndex = items.length - 1;
        }

        // Navigate to the next option
        if (nextIndex >= 0) {
            items[nextIndex]._setFocused(true);
            this._selectedOption = items[nextIndex];
            return this._selectedOption.value;
        } else { // or restore the saved value
            //this._applyValue(this._savedValue); TODO: this is not needed, is it ? If so, the key must be saved as well because applyValue must be passed the key, not the label.
            return this._savedValue;
        }
    }

    _onKeyDown(event) {
        const key = event.key.replace(/^Arrow/, '');
        switch (key) {
            case 'Down':
                event.preventDefault();
                this._openOverlay();
                this._navigate('next');
                break;
            case 'Up':
                event.preventDefault();
                this._openOverlay();
                this._navigate('prev');
                break;
            case 'Enter':
                if (this._selectedOption) {
                    this._applyValue(this._selectedOption.dataset.key);
                } else if (this._optionsToDisplay.length == (1 + (this._hasDefaultOption() ? 1 : 0))) {
                    this._applyValue(this._optionsToDisplay[(this._hasDefaultOption() ? 1 : 0)].key);
                } else if ( this._hasDefaultOption() && this._optionsToDisplay.length == 1 ) {
                    this._applyValue(this._optionsToDisplay[0].key);
                } else if (this.inputValue.length > 0 && !this.loading) {
                    this.dispatchEvent(
                        new CustomEvent('vcf-autosuggest-custom-value-submit', {
                            bubbles: true,
                            detail: {
                                numberOfAvailableOptions: this._optionsToDisplay.length,
                                value: this.inputValue
                            }
                        })
                    );
                }
                break;
            case 'Tab':
            case 'Esc':
            case 'Escape':
                this._cancelEvent(event);
                this._applyValue(this.selectedValue == null ? (this._hasDefaultOption() ? this.defaultOption.key : '') : this.selectedValue);
                this._textFieldFocus(false);
                this.opened = false;
                break;
            case ' ':
                if (this._selectedOption) {
                    this._cancelEvent(event);
                    this._applyValue(this._selectedOption.dataset.key);
                }
                break;
        }
    }

    _optionsToDisplayChanged(otd, opened) {
        if (this.customItemTemplate) {
           this._renderOptionsCustomTemplateIfApplicable();
        }
    }

    _renderOptionsCustomTemplateIfApplicable() {
        if (!this.customItemTemplate || !this.opened) return;
        let listbox = null;
        if (!this._overlayElement) return;
        for(let i=0; i < this._overlayElement.children.length; i++) {
            if (this._overlayElement.children[i].id == "optionsContainer") {
                listbox = this._overlayElement.children[i];
                break;
            }
        }
        if (listbox==null) return;
        let foundCount = 0;
        for(let i=0; i < listbox.children.length; i++) {
            if (listbox.children[i].dataset.tag && listbox.children[i].dataset.tag == 'autosuggestOverlayItem') {
                foundCount++;
                let oid = listbox.children[i].dataset.oid;
                let option = this._optionsToDisplay.filter(o => o.optId==oid)[0]
                var _this = this;
                if (option) listbox.children[i].innerHTML = eval(`_this.__customItemTemplateGenerator = ${this.customItemTemplate}(option, this)`)
            }
        }
        let that = this;

        if (!(foundCount>=this._optionsToDisplay.length)) setTimeout(function(){
            that._renderOptionsCustomTemplateIfApplicable();
        }, 250);
    }
}

customElements.define(VcfAutosuggest.is, VcfAutosuggest);
