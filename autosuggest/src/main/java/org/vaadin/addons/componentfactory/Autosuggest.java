package org.vaadin.addons.componentfactory;

/*
 * #%L
 * VCF Enhanced Combobox for Vaadin 14+
 * %%
 * Copyright (C) 2021 Vaadin Ltd
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * #L%
 */

import com.vaadin.flow.component.*;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.dependency.CssImport;
import com.vaadin.flow.component.dependency.JsModule;
import com.vaadin.flow.component.dependency.NpmPackage;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.FlexLayout;
import com.vaadin.flow.component.polymertemplate.EventHandler;
import com.vaadin.flow.component.polymertemplate.Id;
import com.vaadin.flow.component.polymertemplate.PolymerTemplate;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.value.ValueChangeMode;
import com.vaadin.flow.dom.Element;
import com.vaadin.flow.shared.Registration;
import com.vaadin.flow.templatemodel.TemplateModel;
import lombok.*;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Server-side component for the <code>vcf-autosuggest</code> element.
 * <p>
 * Note: isOpened,setOpened and setValue are not supported. The current
 * implementation of the polymer-side component does not allow it.
 *
 * @author Vaadin Ltd
 */

@Tag("vcf-autosuggest")
@NpmPackage(value = "@vaadin-component-factory/vcf-autosuggest", version = "1.0.7")
@JsModule("@vaadin-component-factory/vcf-autosuggest/src/vcf-autosuggest.js")
@CssImport(value = "@vaadin-component-factory/vcf-autosuggest/styles/style.css")
public class Autosuggest<T> extends PolymerTemplate<Autosuggest.AutosuggestTemplateModel>
        implements HasTheme, HasSize, HasValue<Autosuggest.AutosuggestValueAppliedEvent, String>, Focusable<Autosuggest>, HasValidation {

    /**
     * This model binds properties {@link Autosuggest} and
     * vcf-autosuggest.html
     */
    public interface AutosuggestTemplateModel extends TemplateModel {
        @AllArgsConstructor
        class FOption {
            @Getter
            @Setter
            String label;

            @Getter
            @Setter
            String searchStr;
        }

        List<FOption> getOptions();
        List<FOption> getOptionsForWhenValueIsNull();
        String getPlaceholder();
        Boolean getOpenDropdownOnClick();
        Boolean getReadOnly();
        Integer getLimit();
        String getLabel();
        Boolean getLazy();
        Boolean getCaseSensitive();
        String getSearchMatchingMode();
        Boolean getCustomizeOptionsForWhenValueIsNull();
        String getDefaultValue();
        Boolean getDisableSearchHighlighting();
        Boolean getLoading();
        String getCustomItemTemplate();
        void setLoading(Boolean loading);
        void setOptions(List<FOption> options);
        void setOptionsForWhenValueIsNull(List<FOption> options);
        void setPlaceholder(String placeholder);
        void setOpenDropdownOnClick(Boolean openDropdownOnClick);
        void setReadOnly(Boolean readOnly);
        void setLimit(Integer limit);
        void setLabel(String label);
        void setLazy(Boolean lazy);
        void setCaseSensitive(Boolean caseSensitive);
        void setSearchMatchingMode(String smm);
        void setCustomizeOptionsForWhenValueIsNull(Boolean v);
        void setDefaultValue(String v);
        void setDisableSearchHighlighting(Boolean v);
        void setCustomItemTemplate(String tpl);
    }

    class Option extends AutosuggestTemplateModel.FOption {
        @Getter
        @Setter
        T item;

        public Option(String label, String searchStr,T item) {
            super(label, searchStr);
            this.item = item;
        }
    }

    public interface LazyProviderFunction<T> {}

    public interface LazyProviderFunctionSimple<T> extends LazyProviderFunction<T> {
        List<T> refresh(String searchQ);
    }

    public interface LazyProviderFunctionMap<T> extends LazyProviderFunction<T> {
        Map<String, T> refresh(String searchQ);
    }

    public interface LabelGenerator<T> {
        String generate(T obj);
    }

    public interface SearchStringGenerator<T> {
        String generate(T obj);
    }

    @Setter
    private boolean showClearButton = true;

    @Getter
    private Map<String, Option> items = new HashMap<>();

    @Getter
    private Map<String, Option> itemsForWhenValueIsNull = new HashMap<>();

    @Id
    @Getter
    private TextField textField;

    private LabelGenerator<T> labelGenerator = null;
    private SearchStringGenerator<T> searchStringGenerator = null;

    @Id(value = "autosuggestOverlay")
    private Element overlay;

    @Id(value = "dropdownEndSlot")
    private Element dropdownEndSlot;

    private FlexLayout inputPrefix;
    private FlexLayout inputSuffix;
    private Button clearButton;

    private Registration lazyInputChangeER;

    /**
     * Constructor that sets the maximum number of displayed options.
     *
     * @param limit
     *            maximum number of displayed options
     */
    public Autosuggest(int limit) { //TODO: TEST THE LIMIT PROPERTY
        this();
        setLimit(limit);
    }

    public Autosuggest() {
        this(false);
    }

    /**
     * Default constructor.
     */

    public Autosuggest(boolean placeClearButtonFirst) {
        textField.setSizeFull();
        textField.setValueChangeMode(ValueChangeMode.ON_CHANGE);

        // Init clear button
        clearButton = new Button(new Icon("lumo:cross"), buttonClickEvent -> getElement().executeJs("this.clear()"));
        clearButton.getElement().getThemeList().add("icon");
        clearButton.getElement().getThemeList().add("tertiary");
        clearButton.getElement().getThemeList().add("small");
        clearButton.getElement().setAttribute("aria-label", "Add new item");
        clearButton.getElement().getStyle().set("display", "none");
        clearButton.setId("button-clear");
        addValueChangeListener(valueChangeEvent -> {
            if(showClearButton && valueChangeEvent.value != null && !valueChangeEvent.value.isEmpty() && !isReadOnly()) {
                clearButton.getElement().getStyle().set("display", "block");
            } else {
                clearButton.getElement().getStyle().set("display", "none");
            }
        });

        // Init input prefix
        inputPrefix = new FlexLayout();
        inputPrefix.setAlignItems(FlexComponent.Alignment.CENTER);
        inputPrefix.setJustifyContentMode(FlexComponent.JustifyContentMode.CENTER);
        inputPrefix.getElement().setAttribute("slot", "prefix");
        textField.getElement().appendChild(inputPrefix.getElement());

        // Init input suffix
        inputSuffix = new FlexLayout();
        inputSuffix.setAlignItems(FlexComponent.Alignment.CENTER);
        inputSuffix.setJustifyContentMode(FlexComponent.JustifyContentMode.CENTER);
        FlexLayout inputSuffixContainer = placeClearButtonFirst ? new FlexLayout(clearButton, inputSuffix) : new FlexLayout(inputSuffix, clearButton);
        inputSuffixContainer.setAlignItems(FlexComponent.Alignment.CENTER);
        inputSuffixContainer.setJustifyContentMode(FlexComponent.JustifyContentMode.CENTER);
        inputSuffixContainer.getElement().setAttribute("slot", "suffix");
        textField.getElement().appendChild(inputSuffixContainer.getElement());
    }

    @EventHandler
    public void clear() {
        fireEvent(new ValueClearEvent(this, true));
    }

    public void setInputPrefix(Component... components) {
        inputPrefix.removeAll();
        inputPrefix.add(components);
    }

    public void setInputSuffix(Component... components) {
        inputSuffix.removeAll();
        inputSuffix.add(components);
    }

    public void clearDropdownEndSlot() {
        dropdownEndSlot.removeAllChildren();
        dropdownEndSlot.getStyle().set("display", "none");
    }

    public void setComponentToDropdownEndSlot(Component component) {
        clearDropdownEndSlot();
        dropdownEndSlot.getStyle().set("display", "block");
        dropdownEndSlot.appendChild(component.getElement());
    }

    public boolean getShowClearButton() {
        return showClearButton;
    }

    public boolean getOpenDropdownOnClick() {
        return getModel().getOpenDropdownOnClick();
    }

    public void setOpenDropdownOnClick(boolean v) {
        getModel().setOpenDropdownOnClick(v);
    }

    public void setLoading(boolean loading) {
        getModel().setLoading(loading);
    }

    public Boolean isCaseSensitive() {
        return getModel().getCaseSensitive();
    }

    public void setCaseSensitive(boolean v) {
        getModel().setCaseSensitive(v);
    }

    public Boolean isLazy() {
        return getModel().getLazy();
    }

    public void setLazy(boolean lazy) {
        textField.setValueChangeMode(lazy ? ValueChangeMode.LAZY : ValueChangeMode.ON_CHANGE);
        getModel().setLazy(lazy);
    }

    public SearchMatchingMode getSearchMatchingMode() {
        return SearchMatchingMode.valueOf(getModel().getSearchMatchingMode());
    }

    public void setSearchMatchingMode(SearchMatchingMode smm) {
        getModel().setSearchMatchingMode(smm.toString());
    }

    /**
     * Gets the placeholder.
     * <p>
     * A placeholder string in addition to the label.
     * <p>
     * This property is not synchronized automatically from the client side, so
     * the returned value may not be the same as in client side.
     *
     * @return the {@code placeholder} property from the webcomponent
     */
    public String getPlaceholder() {
        return getModel().getPlaceholder();
    }

    /**
     * Sets the placeholder.
     * <p>
     * Description copied from corresponding location in WebComponent:
     * <p>
     * A placeholder string in addition to the label.
     *
     * @param placeholder
     *            the String value to set
     */
    public void setPlaceholder(String placeholder) {
        getModel().setPlaceholder(placeholder);
    }

    @Override
    public void setReadOnly(boolean readOnly) {
        getModel().setReadOnly(readOnly);
        if(showClearButton && getValue() != null && !getValue().isEmpty() && !readOnly) {
            clearButton.getElement().getStyle().set("display", "block");
        } else {
            clearButton.getElement().getStyle().set("display", "none");
        }
        textField.setReadOnly(readOnly);
    }

    public String getValue() {
        return getElement().getProperty("selectedValue", null);
    }

    public T getValueItem() {
        String label = getValue();
        if( this.items.containsKey(label) ) return null;
        return this.items.get(label).item;
    }

    /**
     * Gets the the maximum number of displayed options.
     *
     * @return limit maximum number of displayed options. null is returned if
     *         there is not a limit set.
     */
    public int getLimit() {
        return getModel().getLimit();
    }

    /**
     * Sets the limit to the maximum number of displayed options.
     * <p>
     * The limit should be bigger than 0.
     *
     * @param limit
     *            maximum number of displayed options
     */
    public void setLimit(int limit) {
       getModel().setLimit(limit);
    }

    /**
     * Gets the current imputed text from the TxtField.
     *
     * @return value text
     */
    @Synchronize(property = "value", value = "value-changed")
    public String getInputValue() {
        return getElement().getProperty("value", "");
    }

    /**
     * Gets the label.
     * <p>
     * String used for the label element.
     * <p>
     * This property is not synchronized automatically from the client side, so
     * the returned value may not be the same as in client side.
     *
     * @return the {@code label} property from the webcomponent
     */
    public String getLabel() {
        return getModel().getLabel();
    }

    /**
     * Sets the label.
     * <p>
     * String used for the label element.
     *
     * @param label
     *            the String value to set
     */
    public void setLabel(String label) {
        getModel().setLabel(label == null ? "" : label);
    }

    public Boolean getCustomizeItemsForWhenValueIsNull() {
        return getModel().getCustomizeOptionsForWhenValueIsNull();
    }

    public void setCustomizeItemsForWhenValueIsNull(boolean v) {
        getModel().setCustomizeOptionsForWhenValueIsNull(v);
    }

    public Registration addEagerInputChangeListener(ComponentEventListener<AucompleteChangeEvent> listener) {
        return addListener(AucompleteChangeEvent.class, listener);
    }

    public Registration addInputChangeListener(ValueChangeListener listener) {
        return textField.addValueChangeListener(listener);
    }

    /**
     * Adds a listener for {@code AutosuggestValueAppliedEvent} events fired by
     * the webcomponent.
     *
     * @param listener
     *            the listener
     * @return a {@link Registration} for removing the event listener
     */
    public Registration addAutosuggestValueAppliedListener(
            ComponentEventListener<AutosuggestValueAppliedEvent> listener) {
        return addListener(AutosuggestValueAppliedEvent.class, listener);
    }

    public void clearDefaultOptionValue() {
        getModel().setDefaultValue("");
    }

    public void setDefaultOptionValue(String val) {
        getModel().setDefaultValue(val);
    }

    /**
     * Adds a listener for {@code ValueClearEvent}.
     *
     * @param listener
     *            the listener
     * @return a {@link Registration} for removing the event listener
     */
    public Registration addValueClearListener(
            ComponentEventListener<ValueClearEvent> listener) {
        return addListener(ValueClearEvent.class, listener);
    }

    public void setLazyProviderSimple(LazyProviderFunctionSimple<T> ff) {
        if(lazyInputChangeER!=null) lazyInputChangeER.remove();
        if(getModel().getLazy()) {
            lazyInputChangeER = addInputChangeListener(valueChangeEvent -> {
                setItems(ff.refresh(getInputValue()));
            });
        }
    }

    public void setLazyProviderMap(LazyProviderFunctionMap<T> ff) {
        if(lazyInputChangeER!=null) lazyInputChangeER.remove();
        if(getModel().getLazy()) {
            lazyInputChangeER = addInputChangeListener(valueChangeEvent -> {
                setItems(ff.refresh(getInputValue()));
            });
        }
    }

    public void unsetLabelGenerator() {
        this.labelGenerator = null;

        Map<String, T> generated = new HashMap<>();
        if(this.items != null) {
            this.items.values().stream().forEach(item -> generated.put(item.toString(), item.item));
        }
        this.setItems(generated);
    }

    public void setLabelGenerator(LabelGenerator<T> lblG) {
        this.labelGenerator = lblG;

        Map<String, T> generated = new HashMap<>();
        if(this.items != null) {
            this.items.values().stream().forEach(item -> generated.put(this.labelGenerator.generate(item.item), item.item));
        }
        this.setItems(generated);
    }

    public void clearSearchStringGenerator() {
        this.searchStringGenerator = null;
        getModel().setDisableSearchHighlighting(false);

        if(this.items != null) {
            this.items.keySet().stream().forEach(key -> {
                Option item = this.items.get(key);
                item.setSearchStr(item.getLabel());
                this.items.put(key, item);
            });
        }
    }

    public void setSearchStringGenerator(SearchStringGenerator<T> searchStringGenerator) {
        this.searchStringGenerator = searchStringGenerator;
        getModel().setDisableSearchHighlighting(true);

        Map<String, T> generated = new HashMap<>();
        if(this.items != null) {
            this.items.values().stream().forEach(item -> {
                generated.put(item.getLabel(), item.getItem());
            });
        }
        this.setItems(generated);
    }

    public void clearItemsForWhenValueIsNull() {
        getModel().setCustomizeOptionsForWhenValueIsNull(false);
        this.itemsForWhenValueIsNull = new HashMap<>();
        getModel().setOptionsForWhenValueIsNull(new ArrayList<>());
    }

    public void setItemsForWhenValueIsNull(Collection<T> items) {
        this.itemsForWhenValueIsNull.clear();
        items.stream().forEach(item -> {
            String label;
            String searchStr;

            //..... templaterenderer ?

            if(this.labelGenerator != null) {
                label = this.labelGenerator.generate(item);
            } else {
                label = item.toString();
            }

            if(this.searchStringGenerator != null) {
                searchStr = this.searchStringGenerator.generate(item);
            } else searchStr = label;

            this.itemsForWhenValueIsNull.put(label, new Option(label, searchStr, item));
        });

        getModel().setCustomizeOptionsForWhenValueIsNull(true);
        getModel().setOptionsForWhenValueIsNull(this.itemsForWhenValueIsNull.values().stream().map(option -> (AutosuggestTemplateModel.FOption) option).collect(Collectors.toList()));
    }

    public void setItemsForWhenValueIsNull(Map<String, T> items) {
        this.itemsForWhenValueIsNull.clear();
        items.keySet().stream().forEach(key -> {
            T item = items.get(key);
            String label;
            String searchStr;

            //..... templaterenderer ?

            if(this.labelGenerator != null) {
                label = this.labelGenerator.generate(item);
            } else {
                label = item.toString();
            }

            if(this.searchStringGenerator != null) {
                searchStr = this.searchStringGenerator.generate(item);
            } else searchStr = label;

            this.itemsForWhenValueIsNull.put(label, new Option(label, searchStr, item));
        });

        getModel().setCustomizeOptionsForWhenValueIsNull(true);
        getModel().setOptionsForWhenValueIsNull(this.itemsForWhenValueIsNull.values().stream().map(option -> (AutosuggestTemplateModel.FOption) option).collect(Collectors.toList()));
    }

    public void clearTemplateProvider() {
        getModel().setCustomItemTemplate(null);
    }

    public void setTemplateProvider(String template) {
        getModel().setCustomItemTemplate(template);
    }

    public void setItems(Collection<T> items) {
        this.items.clear();
        items.stream().forEach(item -> {
            String label;
            String searchStr;

            //..... templaterenderer ?

            if(this.labelGenerator != null) {
                label = this.labelGenerator.generate(item);
            } else {
                label = item.toString();
            }

            if(this.searchStringGenerator != null) {
                searchStr = this.searchStringGenerator.generate(item);
            } else searchStr = label;

            this.items.put(label, new Option(label, searchStr, item));
        });
        getModel().setOptions(this.items.values().stream().map(option -> (AutosuggestTemplateModel.FOption) option).collect(Collectors.toList()));
        //TODO: RESET SELECTION (call clear before setting the options?)
        setLoading(false);
        getElement().executeJs("this._loadingChanged(false)");
    }

    public void setItems(Map<String, T> items) {
        this.items.clear();
        items.keySet().stream().forEach(key -> {
            T item = items.get(key);
            String label;
            String searchStr;

            //..... templaterenderer ?

            if(this.labelGenerator != null) {
                label = this.labelGenerator.generate(item);
            } else {
                label = key;
            }

            if(this.searchStringGenerator != null) {
                searchStr = this.searchStringGenerator.generate(item);
            } else searchStr = label;

            this.items.put(label, new Option(label, searchStr, item));
        });

        //TODO: RESET SELECTION (call clear before setting the options?)
        getModel().setOptions(this.items.values().stream().map(option -> (AutosuggestTemplateModel.FOption) option).collect(Collectors.toList()));
        setLoading(false);
        getElement().executeJs("this._loadingChanged(false)");
    }

    public void setValue(String value) {
        getElement().executeJs("this._applyValue(\"" + value + "\");");
        //TODO: CHECK IF VALUE EXISTS. ATT FOR CASE SENSIT
    }

    public void setValueItem(T item) {
        //TODO
    }

    /**
     * ValueClearEvent is created when the user clicks on the clean button.
     */
    @DomEvent("clear")
    public static class ValueClearEvent extends ComponentEvent<Autosuggest> {
        public ValueClearEvent(Autosuggest source, boolean fromClient) {
            super(source, fromClient);
        }
    }

    @Override
    public boolean isReadOnly() {
        return textField.isReadOnly();
    }

    @Override
    public void setRequiredIndicatorVisible(boolean requiredIndicatorVisible) {
        textField.setRequiredIndicatorVisible(requiredIndicatorVisible);
    }

    @Override
    public boolean isRequiredIndicatorVisible() {
        return textField.isRequiredIndicatorVisible();
    }

    @Override
    public Registration addValueChangeListener(
            ValueChangeListener<? super AutosuggestValueAppliedEvent> listener) {
        return addAutosuggestValueAppliedListener(event -> {
            listener.valueChanged(event);
        });
    }

    @Override
    public String getErrorMessage() {
        if (textField != null) {
            return textField.getErrorMessage();
        } else {
            return null;
        }
    }

    @Override
    public boolean isInvalid() {
        if (textField != null) {
            return textField.isInvalid();
        } else {
            return false;
        }
    }

    @Override
    public void setErrorMessage(String errorMessage) {
        if (textField != null) {
            textField.setErrorMessage(errorMessage);
        }
    }

    @Override
    public void setInvalid(boolean invalid) {
        if (textField != null) {
            textField.setInvalid(invalid);
        }
    }

    @Override
    public void focus() {
        if (textField != null) {
            textField.focus();
        }
    }

    @Override
    public void blur() {
        if (textField != null) {
            textField.blur();
        }
    }

    @Override
    public void setTabIndex(int tabIndex) {
        if (textField != null) {
            textField.setTabIndex(tabIndex);
        }
    }

    @Override
    public int getTabIndex() {
        return textField.getTabIndex();
    }

    @Override
    public ShortcutRegistration addFocusShortcut(Key key,
                                                 KeyModifier... keyModifiers) {
        return textField.addFocusShortcut(key, keyModifiers);
    }

    /**
     * ValueChangeEvent is created when the value of the TextField changes.
     */
    @DomEvent("value-changed")
    public static class AucompleteChangeEvent extends ComponentEvent<Autosuggest> {
        private final String value;

        public AucompleteChangeEvent(Autosuggest source, boolean fromClient, @EventData("event.detail.value") String value) {
            super(source, fromClient);
            this.value = value;
        }

        public String getValue() {
            return value;
        }
    }
    /**
     * AutosuggestValueAppliedEvent is created when the user clicks on a option
     * of the Autosuggestr.
     */
    @DomEvent("vcf-autosuggest-value-applied")
    public static class AutosuggestValueAppliedEvent extends ComponentEvent<Autosuggest>
            implements ValueChangeEvent<String> {

        private final String value;

        public AutosuggestValueAppliedEvent(Autosuggest source,
                                             boolean fromClient,
                                             @EventData("event.detail.value") String value) {
            super(source, fromClient);
            this.value = value;
            this.source = source;
        }

        public String getValue() {
            return value;
        }

        @Override
        public HasValue getHasValue() {
            // TODO Auto-generated method stub
            return (HasValue) source;
        }

        @Override
        public String getOldValue() {
            // TODO Auto-generated method stub
            return null;
        }

    }

    public enum SearchMatchingMode { STARTS_WITH, CONTAINS }
}