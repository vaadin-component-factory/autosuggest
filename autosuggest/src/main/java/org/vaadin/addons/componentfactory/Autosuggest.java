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

import com.vaadin.flow.component.ClientCallable;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.ComponentEvent;
import com.vaadin.flow.component.ComponentEventListener;
import com.vaadin.flow.component.DomEvent;
import com.vaadin.flow.component.EventData;
import com.vaadin.flow.component.Focusable;
import com.vaadin.flow.component.HasComponents;
import com.vaadin.flow.component.HasSize;
import com.vaadin.flow.component.HasTheme;
import com.vaadin.flow.component.HasValidation;
import com.vaadin.flow.component.HasValue;
import com.vaadin.flow.component.Key;
import com.vaadin.flow.component.KeyModifier;
import com.vaadin.flow.component.ShortcutRegistration;
import com.vaadin.flow.component.Synchronize;
import com.vaadin.flow.component.Tag;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.dependency.CssImport;
import com.vaadin.flow.component.dependency.JsModule;
import com.vaadin.flow.component.dependency.NpmPackage;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.littemplate.LitTemplate;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.FlexLayout;
import com.vaadin.flow.component.template.Id;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.value.ValueChangeMode;
import com.vaadin.flow.dom.Element;
import com.vaadin.flow.dom.ThemeList;
import com.vaadin.flow.internal.JsonSerializer;
import com.vaadin.flow.shared.Registration;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
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
@NpmPackage(value = "@vaadin-component-factory/vcf-autosuggest", version = "1.1.5")
@JsModule("@vaadin-component-factory/vcf-autosuggest/src/vcf-autosuggest.js")
//@JsModule("./vcf-autosuggest.js")
@CssImport(value = "@vaadin-component-factory/vcf-autosuggest/styles/style.css")
//@CssImport("./vcf-autosuggest.css")
public class Autosuggest<T> extends LitTemplate implements HasTheme, HasSize, Focusable<Autosuggest<T>>, HasValidation,
    HasComponents
{
    public enum SearchMatchingMode { STARTS_WITH, CONTAINS }

    public static class FOption {
        private final String key;
        private final String label;
        private final String searchStr;

        public FOption(String key, String label, String searchStr) {
            this.key = key;
            this.label = label;
            this.searchStr = searchStr;
        }

        public String getKey() { return this.key; }
        public String getLabel() { return this.label; }
        public String getSearchStr() { return this.searchStr; }
    }

    public class Option {
        private final FOption fOption;
        private final T item;

        public Option(String key, String label, String searchStr,T item) {
            this.fOption = new FOption(key, label, searchStr);
            this.item = item;
        }

        public T getItem() { return this.item; }
        public FOption getFOption() { return this.fOption; }
        public String getKey() { return this.fOption.getKey(); }
        public String getLabel() { return this.fOption.getLabel(); }
        public String getSearchStr() { return this.fOption.getSearchStr(); }
    }

    public interface LazyProviderFunction<T> {}

    public interface LazyProviderFunctionSimple<T> extends LazyProviderFunction<T> {
        List<T> refresh(String searchQ);
    }

    public interface LazyProviderFunctionMap<T> extends LazyProviderFunction<T> {
        Map<String, T> refresh(String searchQ);
    }

    public interface KeyGenerator<T> {
        String generate(T obj);
    }

    public interface LabelGenerator<T> {
        String generate(T obj);
    }

    public interface SearchStringGenerator<T> {
        String generate(T obj);
    }

    private boolean showClearButton = true;
    public void setShowClearButton(Boolean v) { this.showClearButton = v; }

    private Map<String, Option> items = new HashMap<>();
    public Map<String, Option> getItems() { return this.items; }

    private Map<String, Option> itemsForWhenValueIsNull = new HashMap<>();
    public Map<String, Option> getItemsForWhenValueIsNull() { return this.itemsForWhenValueIsNull; }

    @Id("textField")
    private TextField textField;
    public TextField getTextField() { return this.textField; }

    @Id("dropdownEndSlot")
    private Element dropdownEndSlot;

    private KeyGenerator<T> keyGenerator = null;
    private LabelGenerator<T> labelGenerator = null;
    private SearchStringGenerator<T> searchStringGenerator = null;

    private FlexLayout inputPrefix;
    private FlexLayout inputSuffix;
    private Button clearButton;

    private Registration inputTextChangeEvent;
    private Registration selectionEvent;
    private Registration lazyDataRequestEventH;

    /**
     * Constructor that sets the maximum number of displayed options.
     *
     * @param limit maximum number of displayed options
     */
    public Autosuggest(int limit) {
        this();
        setLimit(limit);
    }

    public Autosuggest() {
        this(false);
    }

    /**
     * Default constructor.
     *
     * @param placeClearButtonFirst Should the clear button be placed before the suffix
     */
    public Autosuggest(boolean placeClearButtonFirst) {

        textField.setSizeFull();
        textField.setValueChangeMode(ValueChangeMode.ON_CHANGE);

        // Init clear button
        initClearButton();

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

        setNoResultsMsg("No results");
        setInputLengthBelowMinimumMsg("Please keep typing to trigger search ...");
    }

    /** Init clear button */
    private void initClearButton() {
        Icon clearIcon = new Icon("lumo:cross");
        clearIcon.getElement().getStyle().set("color", "var(--lumo-contrast-70pct)");
        clearButton = new Button(clearIcon, buttonClickEvent -> getElement().executeJs("this.clear()"));
        clearButton.setId("button-clear");
        clearButton.getElement().setAttribute("aria-label", "");
        clearButton.getElement().getStyle()
            .set("display", "none")
            .set("font-size", "var(--lumo-icon-size-m)")
            .set("padding", "0");
        ThemeList themeList = clearButton.getElement().getThemeList();
        themeList.add("icon");
        themeList.add("tertiary");
        themeList.add("small");
        addValueChangeListener(valueChangeEvent -> setClearButtonDisplayStyle(isReadOnly()));
    }

    @ClientCallable
    public void clear() {
        fireEvent(new ValueClearEvent(this, true));
    }

    public void setNoResultsMsg(String msg) {
        getElement().callJsFunction("setNoResultsMessage", msg);
    }

    public void setInputLengthBelowMinimumMsg(String msg) {
        getElement().callJsFunction("setInputLengthBelowMinimumMessage", msg);
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
        if (dropdownEndSlot != null)
            dropdownEndSlot.removeAllChildren();
    }

    public void setComponentToDropdownEndSlot(Component component) {
        clearDropdownEndSlot();
        if (dropdownEndSlot != null)
            dropdownEndSlot.appendChild(component.getElement());
    }

    public boolean getShowClearButton() {
        return showClearButton;
    }

    public boolean getOpenDropdownOnClick() {
        return getElement().getProperty("openDropdownOnClick", false);
    }

    public void setOpenDropdownOnClick(boolean v) {
        getElement().setProperty("openDropdownOnClick", v);
    }

    public void setLoading(boolean loading) {
        getElement().setProperty("loading", loading);
    }

    public void setCaseSensitive(boolean caseSensitive) {
        getElement().setProperty("caseSensitive", caseSensitive);
    }

    /**
     * Update the valuechangemode and set the corresponding listeners
     *
     * @param lazy
     */
    public void setLazy(boolean lazy) {
        textField.setValueChangeMode(lazy ? ValueChangeMode.LAZY : ValueChangeMode.ON_CHANGE);
        getElement().setProperty("lazy", lazy);
        if (inputTextChangeEvent != null) inputTextChangeEvent.remove();
        if (selectionEvent != null) selectionEvent.remove();
        if (lazy) {
            inputTextChangeEvent = addInputChangeListener(valueChangeEvent -> {
                if (!valueChangeEvent.isFromClient()) {
                    if (valueChangeEvent.getValue() == null || valueChangeEvent.getValue().toString().isEmpty())
                        getElement().executeJs("this.clear();");
                    setLoading(false);
                } else if ((valueChangeEvent.getValue() == null) ||
                    (valueChangeEvent.getValue().toString().isEmpty()) ||
                    (getItemForLabel(valueChangeEvent.getValue().toString()).isPresent()))
                {
                    setLoading(false);
                } else if (valueChangeEvent.getValue().toString().trim().length() >= getMinimumInputLengthToPerformLazyQuery())
                    getEventBus().fireEvent(new AutosuggestLazyDataRequestEvent(this, true, valueChangeEvent.getValue().toString()));
            });
            selectionEvent = addValueAppliedListener(autosuggestValueAppliedEvent -> textField.setValue(autosuggestValueAppliedEvent.getLabel()));
        }
    }

    public void setSearchMatchingMode(SearchMatchingMode smm) {
        getElement().setProperty("searchMatchingMode", smm.toString());
    }

    public int getMinimumInputLengthToPerformLazyQuery() {
        return getElement().getProperty("minimumInputLengthToPerformLazyQuery", 0);
    }

    public void setMinimumInputLengthToPerformLazyQuery(Integer minLength) {
        getElement().setProperty("minimumInputLengthToPerformLazyQuery", minLength);
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
        return textField.getPlaceholder();
    }

    /**
     * Sets the placeholder.
     * <p>
     * Description copied from corresponding location in WebComponent:
     * <p>
     * A placeholder string in addition to the label.
     *
     * @param placeholder the String value to set
     */
    public void setPlaceholder(String placeholder) {
        textField.setPlaceholder(placeholder);
    }

    public boolean isReadOnly() {
        return textField.isReadOnly();
    }

    public void setReadOnly(boolean readOnly) {
        setClearButtonDisplayStyle(readOnly);
        textField.setReadOnly(readOnly);
        getElement().setProperty("readOnly", readOnly);
    }

    private void setClearButtonDisplayStyle(boolean readOnly) {
        clearButton.getElement().getStyle()
            .set("display", showClearButton && getValueKey() != null && !getValueKey().isEmpty() && !readOnly
                ? "block" : "none");
    }

    /**
     * Sets the limit to the maximum number of displayed options.
     * <p>
     * The limit should be bigger than 0.
     *
     * @param limit maximum number of displayed options
     */
    public void setLimit(int limit) {
        getElement().setProperty("limit", limit);
    }

    @Synchronize(property = "inputValue", value = "vcf-autosuggest-input-value-changed")
    public String getInputValue() {
        return getElement().getProperty("inputValue", null);
    }

    @Synchronize(property = "selectedValue", value = "vcf-autosuggest-value-applied")
    public String getValueKey() {
        return getElement().getProperty("selectedValue", null);
    }

    public T getValue() {
        String key = getElement().getProperty("selectedValue", null);
        if (this.items.containsKey(key)) return this.items.get(key).item;
        return null;
    }

    public void setValueByKey(String value) {
        if (!this.items.containsKey(value)) throw new IllegalArgumentException("No item found with key " + value);
        applyValue(value);
    }

    public void setValueByLabel(String label) {
        Option option = getItemForLabel(label).orElseThrow(() -> new IllegalArgumentException("No item found with key " + label));
        applyValue(option.getKey());
    }

    public void setValue(T item) {
        if (item == null || !contains(item)) {
            getElement().executeJs("this._applyValue(null);");
        } else {
            setValueByKey(getKey(item));
        }
    }

    public boolean contains(T item) {
        return this.items.containsKey(getKey(item));
    }

    private void applyValue(String value) {
        Element element = getElement();
        element.getNode().runWhenAttached(ui -> ui.beforeClientResponse(this,
            context -> element.executeJs("setTimeout(function() { $0._applyValue(\"" + value + "\"); }, 0);", element))
        );
    }

    private Optional<Option> getItemForLabel(String label) {
        return this.items.values().stream().filter(item -> item.getLabel().equals(label)).findFirst();
    }

    private String getKey(T item) {
        return keyGenerator != null ? keyGenerator.generate(item) : item.toString();
    }

    private String getLabel(T item) {
        return labelGenerator != null ? labelGenerator.generate(item) : item.toString();
    }

    private String getSearchStr(T item, String defValue) {
        return searchStringGenerator != null ? searchStringGenerator.generate(item) : defValue;
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
        return textField.getLabel();
    }

    /**
     * Sets the label.
     * <p>
     * String used for the label element.
     *
     * @param label the String value to set
     */
    public void setLabel(String label) {
        textField.setLabel(label == null ? "" : label);
    }

    public Registration addEagerInputChangeListener(ComponentEventListener<EagerInputChangeEvent> listener) {
        return addListener(EagerInputChangeEvent.class, listener);
    }

    public Registration addCustomValueSubmitListener(ComponentEventListener<CustomValueSubmitEvent> listener) {
        return addListener(CustomValueSubmitEvent.class, listener);
    }

    public Registration addInputChangeListener(HasValue.ValueChangeListener listener) {
        return textField.addValueChangeListener(listener);
    }

    public Registration addLazyDataRequestListener(ComponentEventListener<AutosuggestLazyDataRequestEvent> listener) {
        return getEventBus().addListener(AutosuggestLazyDataRequestEvent.class, listener);
    }

    /**
     * Adds a listener for {@code AutosuggestValueAppliedEvent} events fired by the webcomponent.
     *
     * @param listener the listener
     * @return a {@link Registration} for removing the event listener
     */
    public Registration addValueChangeListener(
        ComponentEventListener<AutosuggestValueAppliedEvent> listener) {
        return addListener(AutosuggestValueAppliedEvent.class, listener);
    }

    public void setDefaultOption(String label) {
        setDefaultOption(label, label, label);
    }

    public void setDefaultOption(String key, String label, String searchStr) {
        getElement().setPropertyJson("defaultOption", JsonSerializer.toJson(new FOption(key, label, searchStr)));
        textField.setValue(label);
    }

    /**
     * Adds a listener for {@code ValueClearEvent}.
     *
     * @param listener the listener
     * @return a {@link Registration} for removing the event listener
     */
    public Registration addValueClearListener(
        ComponentEventListener<ValueClearEvent> listener) {
        return addListener(ValueClearEvent.class, listener);
    }

    public void setLazyProviderSimple(LazyProviderFunctionSimple<T> ff) {
        if (lazyDataRequestEventH != null)
            lazyDataRequestEventH.remove();
        lazyDataRequestEventH = addLazyDataRequestListener(event -> setItems(ff.refresh(textField.getValue())));
    }

    public void setLazyProviderMap(LazyProviderFunctionMap<T> ff) {
        if (lazyDataRequestEventH != null)
            lazyDataRequestEventH.remove();
        lazyDataRequestEventH = addLazyDataRequestListener(event -> setItems(ff.refresh(textField.getValue())));
    }

    public void setKeyGenerator(KeyGenerator<T> keyG) {
        this.keyGenerator = keyG;
        setItems();
    }

    public void unsetKeyGenerator() {
        this.keyGenerator = null;
        setItems();
    }

    public void setLabelGenerator(LabelGenerator<T> lblG) {
        this.labelGenerator = lblG;
        setItems();
    }

    public void unsetLabelGenerator() {
        this.labelGenerator = null;
        setItems();
    }

    public void clearSearchStringGenerator() {
        this.searchStringGenerator = null;
        getElement().setProperty("disableSearchHighlighting", false);
        setItems();
    }

    public void setSearchStringGenerator(SearchStringGenerator<T> searchStringGenerator) {
        this.searchStringGenerator = searchStringGenerator;
        getElement().setProperty("disableSearchHighlighting", true);
        setItems();
    }

    public void clearOptionTemplate() {
        getElement().setProperty("customItemTemplate", null);
    }

    public void setOptionTemplate(String template) { //Available to replace: ${domItem}, ${option}
        String customItemTemplate = "function(option, domItem) { return `" + template + "`; }";
        getElement().setProperty("customItemTemplate", customItemTemplate);
    }

    public void clearItemsForWhenValueIsNull() {
        this.itemsForWhenValueIsNull = new HashMap<>();
        getElement().setPropertyJson("optionsForWhenValueIsNull",
            JsonSerializer.toJson(List.of())
        );
    }

    public void setItemsForWhenValueIsNull(Collection<T> items) {
        this.itemsForWhenValueIsNull.clear();
        this.itemsForWhenValueIsNull.putAll(items.stream().collect(Collectors.toMap(this::getKey, this::getOption)));
        getElement().setPropertyJson("optionsForWhenValueIsNull",
            JsonSerializer.toJson(this.itemsForWhenValueIsNull.values().stream().map(Option::getFOption).collect(Collectors.toList()))
        );
    }

    public void setItemsForWhenValueIsNull(Map<String, T> items) {
        this.itemsForWhenValueIsNull.clear();
        this.itemsForWhenValueIsNull.putAll(
            items.keySet().stream().collect(Collectors.toMap(key -> key, key -> getOption(items.get(key))))
        );
        getElement().setPropertyJson("optionsForWhenValueIsNull",
            JsonSerializer.toJson(this.itemsForWhenValueIsNull.values().stream().map(Option::getFOption).collect(Collectors.toList()))
        );
    }

    private void setItems() {
        if (!items.isEmpty())
            setItems(items.values().stream().map(Option::getItem).collect(Collectors.toList()));
    }

    public void setItems(Collection<T> items) {
        clearItems();
        this.items.putAll(items.stream().collect(Collectors.toMap(this::getKey, this::getOption)));
        getElement().setPropertyJson("options",
            JsonSerializer.toJson(this.items.values().stream().map(Option::getFOption).collect(Collectors.toList()))
        );
        setLoading(false);
    }

    public void setItems(Map<String, T> items) {
        clearItems();
        this.items.putAll(
            items.keySet().stream().collect(Collectors.toMap(key -> key, key -> getOption(items.get(key))))
        );
        getElement().setPropertyJson("options",
            JsonSerializer.toJson(this.items.values().stream().map(Option::getFOption).collect(Collectors.toList()))
        );
        setLoading(false);
    }

    private void clearItems() {
        this.items.clear();
    }

    private Option getOption(T item) {
        String key = getKey(item);
        String label = getLabel(item);
        String searchStr = getSearchStr(item, label);

        return new Option(key, label, searchStr, item);
    }

    /** ValueClearEvent is created when the user clicks on the clear button. */
    @DomEvent("clear")
    public static class ValueClearEvent extends ComponentEvent<Autosuggest> {
        public ValueClearEvent(Autosuggest source, boolean fromClient) {
            super(source, fromClient);
        }
    }

    public void setRequiredIndicatorVisible(boolean requiredIndicatorVisible) {
        textField.setRequiredIndicatorVisible(requiredIndicatorVisible);
    }

    public boolean isRequiredIndicatorVisible() {
        return textField.isRequiredIndicatorVisible();
    }

    public Registration addValueAppliedListener(ComponentEventListener<AutosuggestValueAppliedEvent> listener) {
        return addListener(AutosuggestValueAppliedEvent.class, listener);
    }

    @Override
    public String getErrorMessage() {
        return textField != null ? textField.getErrorMessage() : null;
    }

    @Override
    public boolean isInvalid() {
        return textField != null && textField.isInvalid();
    }

    @Override
    public void setErrorMessage(String errorMessage) {
        if (textField != null)
            textField.setErrorMessage(errorMessage);
    }

    @Override
    public void setInvalid(boolean invalid) {
        if (textField != null)
            textField.setInvalid(invalid);
    }

    @Override
    public void focus() {
        if (textField != null)
            textField.focus();
    }

    @Override
    public void blur() {
        if (textField != null)
            textField.blur();
    }

    @Override
    public void setTabIndex(int tabIndex) {
        if (textField != null)
            textField.setTabIndex(tabIndex);
    }

    @Override
    public int getTabIndex() {
        return textField.getTabIndex();
    }

    @Override
    public ShortcutRegistration addFocusShortcut(Key key,  KeyModifier... keyModifiers) {
        return textField.addFocusShortcut(key, keyModifiers);
    }

    /** EagerInputChangeEvent is created when the value of the TextField changes. */
    @DomEvent("vcf-autosuggest-input-value-changed")
    public static class EagerInputChangeEvent extends ComponentEvent<Autosuggest> {

        private final String value;

        public EagerInputChangeEvent(Autosuggest source, boolean fromClient, @EventData("event.detail.value") String value) {
            super(source, fromClient);
            this.value = value;
        }

        public String getValue() { return value; }
    }

    /**
     * CustomValueSubmitEvent is created when the enter key is pressed for an option that's not in the list,
     * returning the current value of the TextField
     */
    @DomEvent("vcf-autosuggest-custom-value-submit")
    public static class CustomValueSubmitEvent extends ComponentEvent<Autosuggest> {

        private final String value;
        private final Integer numberOfAvailableOptions;

        public CustomValueSubmitEvent(Autosuggest source, boolean fromClient, @EventData("event.detail.value") String value, @EventData("event.detail.numberOfAvailableOptions") Integer numberOfAvailableOptions) {
            super(source, fromClient);
            this.value = value;
            this.numberOfAvailableOptions = numberOfAvailableOptions;
        }

        public String getValue() { return value; }
        public Integer getNumberOfAvailableOptions() { return numberOfAvailableOptions; }
    }

    /** AutosuggestValueAppliedEvent is created when the user clicks on a option of the Autosuggest. */
    @DomEvent("vcf-autosuggest-value-applied")
    public static class AutosuggestValueAppliedEvent extends ComponentEvent<Autosuggest> {

        private final String label;
        private final String value;

        public AutosuggestValueAppliedEvent(Autosuggest source, boolean fromClient, @EventData("event.detail.value") String value, @EventData("event.detail.label") String label) {
            super(source, fromClient);
            this.value = value;
            this.source = source;
            this.label = label;
        }

        public String getValue() { return value; }
        public String getLabel() { return label; }
    }

    //@DomEvent("vcf-autosuggest-lazy-data-request")
    public static class AutosuggestLazyDataRequestEvent extends ComponentEvent<Autosuggest> {

        private final String value;

        public AutosuggestLazyDataRequestEvent(Autosuggest source, boolean fromClient, @EventData("event.detail.value") String value) {
            super(source, fromClient);
            this.value = value;
            this.source = source;
        }

        public String getValue() { return value; }
    }
}
