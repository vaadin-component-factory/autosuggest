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

import { html, PolymerElement } from '@polymer/polymer/polymer-element';
import { mixinBehaviors } from '@polymer/polymer/lib/legacy/class.js';
import { ThemableMixin } from '@vaadin/vaadin-themable-mixin';
import { ElementMixin } from '@vaadin/vaadin-element-mixin';
import { IronResizableBehavior } from '@polymer/iron-resizable-behavior';
import {} from '@polymer/polymer/lib/utils/flush.js';
import '@vaadin/vaadin-text-field';
import '@vaadin/vaadin-list-box';
import '@vaadin/vaadin-item';
import '@vaadin/vaadin-button';
import '@vaadin/vaadin-lumo-styles/icons';
import '@polymer/iron-icon';
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

 class VcfAutosuggest extends ElementMixin(ThemableMixin(mixinBehaviors([IronResizableBehavior], PolymerElement))) {
    static get template() {
        return html`
            <style>
                .container {
                    padding: 2px;
                }

                :host {
                    display: inline-block;
                }

                :host([opened]) {
                    pointer-events: auto;
                }

                :host([read-only]) {
                    pointer-events: none;
                }
            </style>
            <div class="container">
                <vaadin-text-field id="textField" on-focus="_textFieldFocused" label="[[label]]" placeholder="[[placeholder]]" theme$="[[theme]]"> </vaadin-text-field>
                <vcf-autosuggest-overlay id="autosuggestOverlay" opened="{{opened}}" theme$="[[theme]]">
                    <vaadin-list-box id="optionsContainer" part="options-container" style="margin: 0;">
                        <template is="dom-if" if="[[loading]]" restamp="true">
                            <style>
                                [part='loading-indicator'] {
                                    background: repeating-linear-gradient(to right, var(--lumo-primary-color), var(--lumo-primary-color-50pct) 25%, var(--lumo-primary-color-10pct) 50%, var(--lumo-primary-color-50pct) 75%, var(--lumo-primary-color) 100%);
                                    width: 100%;
                                    background-size: 200% auto;
                                    background-position: 0 100%;
                                    animation: loading-animation 1s infinite;
                                    animation-fill-mode: both;
                                    animation-timing-function: linear;
                                    height: 4px;
                                    border-radius: var(--lumo-border-radius-s);
                                }

                                @keyframes loading-animation {
                                    0%   { background-position: 0 0; }
                                    100% { background-position: -200% 0; }
                                }

                                [part='option'].loading {
                                    padding-left: 0.5em;
                                    padding-right: 0.5em;
                                }
                            </style>

                            <vaadin-item disabled part="option" class="loading">
                                <div part="loading-indicator"></div>
                            </vaadin-item>
                        </template>

                        <template is="dom-if" if="[[_showNoResultsItem]]">
                            <style>
                                [part='option'] {
                                    padding-left: 0.5em;
                                    padding-right: 0.5em;
                                }

                                [part='no-results']::after {
                                    content: var(--x-no-results-msg);
                                }
                            </style>
                            <vaadin-item disabled part="option" class="no-results">
                                <div part="no-results"></div>
                            </vaadin-item>
                        </template>

                        <template is="dom-if" if="[[_showInputLengthBelowMinimumItem]]">
                            <style>
                                [part='option'] {
                                    padding-left: 0.5em;
                                    padding-right: 0.5em;
                                }

                                [part='input-length-below-minimum']::after {
                                    content: var(--x-input-length-below-minimum-msg);
                                }
                            </style>
                            <vaadin-item disabled part="option" class="input-length-below-minimum">
                                <div part="input-length-below-minimum"></div>
                            </vaadin-item>
                        </template>

                        <template is="dom-if" if="[[!loading]]">
                            <template is="dom-repeat" items="[[_optionsToDisplay]]" as="option">
                                <template is="dom-if" if="[[!customItemTemplate]]">
                                    <style>
                                        [part='option'] {
                                            padding-left: 0.5em;
                                            padding-right: 0.5em;
                                        }

                                        [part='bold'] {
                                            font-weight: 600;
                                        }
                                    </style>
                                    <vaadin-item on-click="_optionClicked" part="option" data-oid="{{option.optId}}" data-key="{{option.key}}">
                                        [[_getSuggestedStart(inputValue, option)]]<span part="bold">[[_getInputtedPart(inputValue, option)]]</span>[[_getSuggestedEnd(inputValue, option)]]
                                    </vaadin-item>
                                </template>
                                <template is="dom-if" if="[[customItemTemplate]]">
                                    <style>
                                        [part='option'] {
                                            padding-left: 0.5em;
                                            padding-right: 0.5em;
                                        }
                                    </style>
                                    <vaadin-item on-click="_optionClicked" part="option" data-oid="{{option.optId}}" data-key="{{option.key}}" data-tag="autosuggestOverlayItem"></vaadin-item>
                                </template>
                            </template>
                        </template>
                    </vaadin-list-box>
                    <div id="dropdownEndSlot" part="dropdown-end-slot" style="display: none; padding-left: 0.5em; padding-right: 0.5em;"></div>
                </vcf-autosuggest-overlay>
        `;
    }

    static get is() {
        return 'vcf-autosuggest';
    }

    static get version() {
        return '1.2.3';
    }

    static get properties() {
        return {
            inputValue: { observer: '_inputValueChanged', type: String, notify: true, value: '' },
            selectedValue: { type: String, value: null },
            opened: { observer: '_openedChange', type: Boolean, reflectToAttribute: true, value: false },
            openDropdownOnClick: { type: Boolean, value: false },
            readOnly: { type: Boolean, reflectToAttribute: true, value: false },
            limit: { type: Number, value: null },
            placeholder: { type: String },
            label: { type: String },
            caseSensitive: { type: Boolean, value: false },
            lazy: { type: Boolean, value: false },
            options: { type: Array, value: () => [] },
            searchMatchingMode: { type: String, value: 'STARTS_WITH' },
            customizeOptionsForWhenValueIsNull: { type: Boolean, value: false },
            optionsForWhenValueIsNull: { type: Array, value: () => [] },
            disableSearchHighlighting: { type: Boolean, value: false },
            defaultOption: { type: Object, value: null },
            _optionsToDisplay: { type: Array, value: () => [] },
            _savedValue: { type: String },
            _showNoResultsItem: { type: Boolean, value: false },
            _showInputLengthBelowMinimumItem: { type: Boolean, value: false },
           loading: { type: Boolean, value: false },
           customItemTemplate: { type: String, reflectToAttribute: true, value: null },
           noResultsMsg: { type: String, value: 'No results' },
           minimumInputLengthToPerformLazyQuery: { type: Number, value: 0 },

           _overlayElement: Object,
           _optionsContainer: Object,
           _selectedOption: Object,
           _boundOutsideClickHandler: Object,
           _boundSetOverlayPosition: Object
        };
    }

   /**
    * @protected
    */
    static _finalizeClass() { super._finalizeClass(); }

    static get observers() {
        return [
            '_selectedOptionChanged(_selectedOption)',
            '_defaultOptionChanged(defaultOption)',
            '_refreshOptionsToDisplay(options, value, options.splices)',
            '_optionsToDisplayChanged(_optionsToDisplay, opened)'
       ];
    }

    constructor() {
        super();
        this._boundSetOverlayPosition = this._setOverlayPosition.bind(this);
        this._boundOutsideClickHandler = this._outsideClickHandler.bind(this);
    }

    attached() {
        super.attached();
        if (this._hasDefaultOption())
            this._defaultOptionChanged(this.defaultOption);
    }

    _loadingChanged(v) {
        this.loading = !v
        this.loading = v //FORCE RE-RENDER
        this._refreshMessageItemsState();
    }

    _defaultOptionChanged(o) {
        if(o != null) {
            this._defaultOption = o;
            this.$.textField.value = o.label;
            this._savedValue = null
            this.opened = false;
            this.$.textField.blur();
        }
    }

    _textFieldFocused() {
        if (this.inputValue && this.inputValue.length > 0) {
            this.opened = true;
        }
    }

    _outsideClickHandler() {
        if(!this.opened) return;
        this._applyValue(this.selectedValue == null ? (this._hasDefaultOption() ? this._defaultOption.key : '') : this.selectedValue);
        this.opened = false;
    }

   // -------- Evt. Handlers --------

    connectedCallback() {
        super.connectedCallback();
        document.addEventListener('click', this._boundOutsideClickHandler);
    }

    ready() {
        super.ready();
        this.$.textField.addEventListener('input', this._onInput.bind(this));
        this.addEventListener('iron-resize', this._boundSetOverlayPosition);
        this.addEventListener('click', this._elementClickListener);
        this.addEventListener('blur', this._elementBlurListener);
        this.addEventListener('keydown', this._onKeyDown.bind(this));
        this._overlayElement = this.$.autosuggestOverlay;
        this._optionsContainer = this.$.optionsContainer;
        this._overlayElement.addEventListener('vaadin-overlay-outside-click', ev => ev.preventDefault());
        this._dropdownEndSlot = this.$.dropdownEndSlot;
        this._dropdownEndSlot.addEventListener('click', ev => { ev.preventDefault(); ev.stopPropagation(); });
    }

    disconnectedCallback() {
        super.disconnectedCallback();
        document.removeEventListener('click', this._boundOutsideClickHandler);
    }

    _elementClickListener(event) {
        if(this.openDropdownOnClick) this.opened = true;
        event.stopPropagation();
    }

    _inputValueChanged(value) {
        if (this._selectedOption) {
            this._selectedOption._setFocused(false);
            this._selectedOption = null;
        }
        if (value.length > 0 && !this.opened) this.opened = true;
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

    _optionClicked(ev) {
        this._applyValue(ev.model.option.key);
    }

    _refreshOptionsToDisplay(options, value) {
        if(typeof value === 'undefined') value = null;
        let _res = [];
        if(this.customizeOptionsForWhenValueIsNull && (value == null || value.length == 0 || value.trim() == (this._hasDefaultOption() ? this._defaultOption.label : '').trim()))
            _res = _res.concat(this._limitOptions(this.optionsForWhenValueIsNull));
        else _res = _res.concat(this._limitOptions(this._filterOptions(options, value)));
        if(!_res || _res==null) _res = [];

        // Criteria for showing the default option:
        // 1. The input value is "" and the default value's key is not present in the optionsForWhenValueIsNull list
        // 2. It matches with the default option and the key is not in the results already
        if(this._hasDefaultOption()) {
            if(value.length == 0) {
                if(!this.customizeOptionsForWhenValueIsNull) _res.unshift({label: this._defaultOption.label, searchStr: this._defaultOption.searchStr, key: this._defaultOption.key});
                else if(_res.filter(opt => opt.key == this._defaultOption.key).length == 0) _res.unshift({label: this._defaultOption.label, searchStr: this._defaultOption.searchStr, key: this._defaultOption.key});
            }
            if(value.length > 0 && this._filterOptions([this._defaultOption], value).length > 0 && _res.filter(opt => opt.key == this._defaultOption.key).length == 0)
                _res.unshift({label: this._defaultOption.label, searchStr: this._defaultOption.searchStr, key: this._defaultOption.key});
        }

        for(let i=0; i<_res.length; i++) { _res[i].optId = i; }
        this._optionsToDisplay = _res;
        this._refreshMessageItemsState();
    }

    _hasDefaultOption() {
        return (this._defaultOption != null && this._defaultOption.key != null);
    }

    _limitOptions(options) {
        if(!options) return [];
        if(this.limit != null) return options.slice(0, this.limit);
        else return options;
    }

    _filterOptions(opts, v) {
        if(v == null || v.trim().length == 0 || v.trim() == (this._hasDefaultOption() ? this._defaultOption.label : '').trim()) return opts;
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

    _onKeyDown(event) {
        const key = event.key.replace(/^Arrow/, '');
        switch (key) {
            case 'Down':
                event.preventDefault();
                this.opened = true;
                this._navigate('next');
                break;
            case 'Up':
                event.preventDefault();
                this.opened = true;
                this._navigate('prev');
                break;
            case 'Enter':
                if (this._selectedOption) {
                    this._applyValue(this._selectedOption.dataKey);
                } else if(this._optionsToDisplay.length == (1 + (this._hasDefaultOption() ? 1 : 0))) {
                    this._applyValue(this._optionsToDisplay[(this._hasDefaultOption() ? 1 : 0)].key);
                } else if( this._hasDefaultOption() && this._optionsToDisplay.length == 1 ) {
                    this._applyValue(this._optionsToDisplay[0].key);
                }

                if(this.inputValue.length > 0 && !this.loading) {
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
                this._applyValue(this.selectedValue == null ? (this._hasDefaultOption() ? this._defaultOption.key : '') : this.selectedValue);
                this.$.textField.blur();
                this.opened = false;
                break;
            case ' ':
                if (this._selectedOption) {
                    event.preventDefault();
                    event.stopPropagation();
                    this._applyValue(this._selectedOption.dataKey);
                }
                break;
        }
    }

    _onInput(event) {
        if(event.target != null && event.target.value != null) {
            this.inputValue = event.target.value.trim();
        } else {
            this.inputValue = '';
        }
        this._refreshOptionsToDisplay(this.options, this.inputValue)
        if(this.lazy && this.inputValue.length >= this.minimumInputLengthToPerformLazyQuery) this.loading = true;
        if(this.inputValue.length > 0) this.opened = true;
        this._refreshMessageItemsState();
    }

    _openedChange(opened) {
        if (opened) {
            this._setOverlayPosition();
            this._refreshOptionsToDisplay(this.options, this.inputValue);
            window.addEventListener('scroll', this._boundSetOverlayPosition, true);
            this.$.textField.addEventListener('wheel', this._cancelEvent, true);
        } else {
            window.removeEventListener('scroll', this._boundSetOverlayPosition, true);
            this.$.textField.removeEventListener('wheel', this._cancelEvent, true);
        }
    }

    _selectedOptionChanged(selectedOption) {
        if (!selectedOption) {return;}
        this.$.textField.value = selectedOption.value;
    }

    // -------- Methods --------

    _cancelEvent(ev) {
        ev.preventDefault();
        ev.stopPropagation();
    }

    _setOverlayPosition() {
        const inputRect = this.$.textField.getBoundingClientRect();
        this._overlayElement.style.left = inputRect.left + 'px';
        this._overlayElement.style.top = inputRect.bottom + window.pageYOffset + 'px';
        this._overlayElement.updateStyles({ '--vcf-autosuggest-options-width': inputRect.width + 'px' });
    }

    _refreshMessageItemsState() {
        if( !(this.lazy && this.minimumInputLengthToPerformLazyQuery>0) ) {
            this._showNoResultsItem = this._optionsToDisplay.length == 0 && !this.loading;
            this._showInputLengthBelowMinimumItem = false;
        } else {
            this._showNoResultsItem = this._optionsToDisplay.length == 0 && !this.loading && this.inputValue.length >= this.minimumInputLengthToPerformLazyQuery;
            if(!this._showNoResultsItem && !this.loading && this._optionsToDisplay.length == 0){
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

    _applyValue(value, keepDropdownOpened=false) {
        if(value == null && this._hasDefaultOption()) value = this._defaultOption.key;
        this.selectedValue = (this._hasDefaultOption() && value == this._defaultOption.key ? null : value);

        let optLbl = "";
        let opt = this.options.find(x => x.key == value)
        if(!opt) opt = this.optionsForWhenValueIsNull.find(x => x.key == value)
        if(!opt) opt = this._hasDefaultOption() ? this._defaultOption : null
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
        if(!keepDropdownOpened) {
            this.opened = false;
            this._textFieldFocus(false)
        }
    }

    clear(keepDropdownOpened=false) {
        if(!keepDropdownOpened) this._applyValue(this._hasDefaultOption() ? this._defaultOption.key : '', true);
        this._textFieldFocus();
        if(!keepDropdownOpened) {
            this.opened = false;
            this._textFieldFocus(false);
        }
    }

    _changeTextFieldValue(newValue) {
        if(typeof this.$ !== 'undefined') {
            this.$.textField.value = newValue;

            this.$.textField.dispatchEvent(
                new Event('input', {
                    bubbles: true,
                    cancelable: true
                })
            );

            this.$.textField.dispatchEvent(
                new Event('value-changed', {
                    bubbles: true,
                    cancelable: true
                })
            );

            this.$.textField.dispatchEvent(
                new Event('change', {
                    bubbles: true,
                    cancelable: true
                })
            );
        }
        this._inputValueChanged(newValue);
    }

    _optionsToDisplayChanged(otd, opened) {
        if(this.customItemTemplate) {
           this._renderOptionsCustomTemplateIfApplicable();
        }
    }

    _renderOptionsCustomTemplateIfApplicable() {
        if(!this.customItemTemplate || !this.opened) return;
        let listbox = null;
        if(!this._overlayElement) return;
        for(let i=0; i < this._overlayElement.children.length; i++) {
            if(this._overlayElement.children[i].id == "optionsContainer") {
                listbox = this._overlayElement.children[i];
                break;
            }
        }
        if(listbox==null) return;
        let foundCount = 0;
        for(let i=0; i < listbox.children.length; i++) {
            if(listbox.children[i].dataset.tag && listbox.children[i].dataset.tag == 'autosuggestOverlayItem') {
                foundCount++;
                let oid = listbox.children[i].dataOid;
                let option = this._optionsToDisplay.filter(o => o.optId==oid)[0]
                var _this = this;
                if(option) listbox.children[i].innerHTML = eval(`_this.__customItemTemplateGenerator = ${this.customItemTemplate}(option, this)`)
            }
        }
        let that = this;

        if(!(foundCount>=this._optionsToDisplay.length)) setTimeout(function(){
            that._renderOptionsCustomTemplateIfApplicable();
        }, 250);
    }

    _textFieldFocus(focus=true) {
        if(typeof this.$ === 'undefined') return;
        if (focus)
            this.$.textField.focus();
        else
            this.$.textField.blur();
    }
}

customElements.define(VcfAutosuggest.is, VcfAutosuggest);

/**
* @namespace Vaadin
*/
window.Vaadin.VcfAutosuggest = VcfAutosuggest;
