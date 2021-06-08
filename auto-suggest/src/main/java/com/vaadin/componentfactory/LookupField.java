package org.vaadin.componentfactory;

/*
 * #%L
 * lookup-field-flow
 * %%
 * Copyright (C) 2020 Vaadin Ltd
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

import com.vaadin.componentfactory.EnhancedDialog;
import com.vaadin.componentfactory.theme.EnhancedDialogVariant;
import com.vaadin.flow.component.AbstractField;
import com.vaadin.flow.component.ClientCallable;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.HasHelper;
import com.vaadin.flow.component.HasSize;
import com.vaadin.flow.component.HasStyle;
import com.vaadin.flow.component.HasTheme;
import com.vaadin.flow.component.HasValidation;
import com.vaadin.flow.component.HasValueAndElement;
import com.vaadin.flow.component.ItemLabelGenerator;
import com.vaadin.flow.component.Tag;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.dependency.JsModule;
import com.vaadin.flow.component.dependency.NpmPackage;
import com.vaadin.flow.component.dependency.Uses;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.binder.HasFilterableDataProvider;
import com.vaadin.flow.data.provider.ConfigurableFilterDataProvider;
import com.vaadin.flow.data.provider.DataProvider;
import com.vaadin.flow.data.provider.ListDataProvider;
import com.vaadin.flow.function.SerializableConsumer;
import com.vaadin.flow.function.SerializableFunction;
import com.vaadin.flow.internal.JsonSerializer;
import com.vaadin.flow.shared.Registration;
import elemental.json.JsonObject;

import java.io.Serializable;
import java.util.Collection;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Server-side component for the {@code vcf-lookup-field} webcomponent.
 *
 * The LookupField is a combination of a combobox and a dialog for advanced search.
 *
 *
 * @param <T> the type of the items to be inserted in the combo box
 */
@Uses(value = Icon.class)
@Uses(value = TextField.class)
@Uses(value = Button.class)
@Uses(value = EnhancedDialog.class)
@Tag("vcf-lookup-field")
@JsModule("@vaadin-component-factory/vcf-auto-suggest/src/vcf-lookup-field.js")
public class LookupField<T> extends Div implements HasFilterableDataProvider<T, String>,
        HasValueAndElement<AbstractField.ComponentValueChangeEvent<LookupField<T>, T>, T>, HasValidation, HasHelper, HasSize, HasTheme {

    private static final String FIELD_SLOT_NAME = "field";
    private static final String GRID_SLOT_NAME = "grid";
    private static final String HEADER_SLOT_NAME = "dialog-header";
    private static final String FOOTER_SLOT_NAME = "dialog-footer";
    private static final String SLOT_KEY = "slot";

    private LookupFieldI18n i18n;
    private Grid<T> grid;
    private ComboBox<T> comboBox;
    private ConfigurableFilterDataProvider<T, Void, String> gridDataProvider;
    private Component header;
    private Component footer;
    private Runnable notificationWhenEmptySelection;

    public LookupField() {
        this(new Grid<>(), new ComboBox<>());
    }

    public LookupField(Class<T> beanType) {
        this(new Grid<>(beanType), new ComboBox<>());
    }

    public LookupField(Grid<T> grid, ComboBox<T> comboBox) {
        super();
        setGrid(grid);
        setComboBox(comboBox);
    }

    /**
     * Set the grid
     *
     * @param grid the grid
     */
    public void setGrid(Grid<T> grid) {
        Objects.requireNonNull(grid, "Grid cannot be null");

        if (this.grid != null && this.grid.getElement().getParent() == getElement()) {
            this.grid.getElement().removeFromParent();
        }

        this.grid = grid;
        grid.getElement().setAttribute(SLOT_KEY, GRID_SLOT_NAME);

        // It might already have a parent e.g when injected from a template
        if (grid.getElement().getParent() == null) {
            getElement().appendChild(grid.getElement());
        }
    }

    /**
     * Set the comboBox
     *
     * @param comboBox the comboBox
     */
    public void setComboBox(ComboBox<T> comboBox) {
        Objects.requireNonNull(comboBox, "ComboBox cannot be null");

        if (this.comboBox != null && this.comboBox.getElement().getParent() == getElement()) {
            this.comboBox.getElement().removeFromParent();
        }
        comboBox.setClearButtonVisible(true);
        comboBox.setAllowCustomValue(true);

        this.comboBox = comboBox;
        comboBox.getElement().setAttribute(SLOT_KEY, FIELD_SLOT_NAME);

        // It might already have a parent e.g when injected from a template
        if (comboBox.getElement().getParent() == null) {
            getElement().appendChild(comboBox.getElement());
        }
    }

    /**
     * <p>
     * Filtering will use a case insensitive match to show all items where the
     * filter text is a substring of the label displayed for that item, which
     * you can configure with
     * {@link #setItemLabelGenerator(ItemLabelGenerator)}.
     * <p>
     * @param items the data items to display
     */
    @Override
    public void setItems(Collection<T> items) {
        setDataProvider(DataProvider.ofCollection(items));
    }

    /**
     *
     * @param itemFilter
     *            filter to check if an item is shown when user typed some text
     *            into the ComboBox
     * @param items
     *            the data items to display
     */
    public void setItems(ComboBox.ItemFilter<T> itemFilter, Collection<T> items) {
        ListDataProvider<T> listDataProvider = DataProvider.ofCollection(items);

        setDataProvider(itemFilter, listDataProvider);
    }

    public void setDataProvider(ListDataProvider<T> listDataProvider) {
        ComboBox.ItemFilter<T> defaultItemFilter = (item, filterText) ->
                comboBox.getItemLabelGenerator().apply(item).toLowerCase(getLocale())
                        .contains(filterText.toLowerCase(getLocale()));

        setDataProvider(defaultItemFilter, listDataProvider);
    }

    /**
     * Sets a list data provider with an item filter as the data provider.
     *
     * @param itemFilter
     *            filter to check if an item is shown when user typed some text
     *            into the ComboBox
     * @param listDataProvider
     *            the list data provider to use, not <code>null</code>
     */
    public void setDataProvider(ComboBox.ItemFilter<T> itemFilter,
                                ListDataProvider<T> listDataProvider) {
        Objects.requireNonNull(listDataProvider,
                "List data provider cannot be null");

        setDataProvider(listDataProvider,
                filterText -> item -> itemFilter.test(item, filterText));
    }

    @Override
    public <C> void setDataProvider(DataProvider<T, C> dataProvider,
                                    SerializableFunction<String, C> filterConverter) {
        Objects.requireNonNull(dataProvider, "data provider cannot be null");
        comboBox.setDataProvider(dataProvider, filterConverter);
        gridDataProvider = dataProvider.withConvertedFilter(filterConverter).withConfigurableFilter();
        grid.setDataProvider(gridDataProvider);
    }

    /**
     *
     * @return the internal grid
     */
    public Grid<T> getGrid() {
        return grid;
    }

    /**
     *
     * @return the internal field
     */
    public ComboBox<T> getComboBox() {
        return comboBox;
    }

    /**
     * Copy the selected value of the grid into the field
     */
    @ClientCallable
    private void copyFieldValueFromGrid() {
        grid.getSelectedItems().stream().findFirst().ifPresent(comboBox::setValue);
    }

    /**
     * Copy the selected value of the field into the grid
     */
    @ClientCallable
    private void copyFieldValueToGrid() {
        grid.select(comboBox.getValue());
    }

    /**
     * Filter the grid
     *
     * @param filter filter text
     */
    @ClientCallable
    private void filterGrid(String filter) {
        if (filter != null) {
            gridDataProvider.setFilter(filter);
        }
    }

    /**
     * Sets the item label generator that is used to produce the strings shown
     * in the combo box for each item. By default,
     * {@link String#valueOf(Object)} is used.
     * <p>
     *
     * @param itemLabelGenerator
     *            the item label provider to use, not null
     */
    public void setItemLabelGenerator(ItemLabelGenerator<T> itemLabelGenerator) {
        comboBox.setItemLabelGenerator(itemLabelGenerator);
    }

    /**
     * Set the width of the grid
     * Also set a max width to 100%
     *
     * @param width the width to set, may be {@code null}
     */
    public void setGridWidth(String width) {
        grid.setWidth(width);
        grid.setMaxWidth("100%");
    }

    /**
     * Set the header of the dialog
     *
     * @param header text for the header of the dialog
     */
    public void setHeader(String header) {
        getElement().setAttribute("header", header);
    }

    /**
     * Set the label of the field
     *
     * @param label label of the field
     */
    public void setLabel(String label) {
        comboBox.setLabel(label);
    }

    /**
     * Sets whether component will open modal or modeless dialog.
     * <p>
     * Note: When dialog is set to be modeless, then it's up to you to provide
     * means for it to be closed (eg. a button that calls {@link Dialog#close()}).
     * The reason being that a modeless dialog allows user to interact with the
     * interface under it and won't be closed by clicking outside or the ESC key.
     *
     * @param modal
     *          {@code false} to enable dialog to open as modeless modal,
     *          {@code true} otherwise.
     */
    public void setModal(boolean modal) {
        getElement().setProperty("modeless", !modal);
    }

    /**
     * Gets whether component is set as modal or modeless dialog.
     *
     * @return  {@code true} if modal dialog (default),
     *          {@code false} otherwise.
     */
    public boolean isModal() {
        return !getElement().getProperty("modeless", false);
    }


    /**
     * Sets whether dialog is enabled to be dragged by the user or not.
     * <p>
     * To allow an element inside the dialog to be dragged by the user
     * (for instance, a header inside the dialog), a class {@code "draggable"}
     * can be added to it (see {@link HasStyle#addClassName(String)}).
     * <p>
     * Note: If draggable is enabled and dialog is opened without first
     * being explicitly attached to a parent, then it won't restore its
     * last position in the case the user closes and opens it again.
     * Reason being that a self attached dialog is removed from the DOM
     * when it's closed and position is not synched.
     *
     * @param draggable
     *          {@code true} to enable dragging of the dialog,
     *          {@code false} otherwise
     */
    public void setDraggable(boolean draggable) {
        getElement().setProperty("draggable", draggable);
    }

    /**
     * Gets whether dialog is enabled to be dragged or not.
     *
     * @return
     *      {@code true} if dragging is enabled,
     *      {@code false} otherwise (default).
     */
    public boolean isDraggable() {
        return getElement().getProperty("draggable", false);
    }

    /**
     * Sets whether dialog can be resized by user or not.
     *
     * @param resizable
     *          {@code true} to enabled resizing of the dialog,
     *          {@code false} otherwise.
     */
    public void setResizable(boolean resizable) {
        getElement().setProperty("resizable", resizable);
    }

    /**
     * Gets whether dialog is enabled to be resized or not.
     *
     * @return
     *      {@code true} if resizing is enabled,
     *      {@code false} otherwiser (default).
     */
    public boolean isResizable() {
        return getElement().getProperty("resizable", false);
    }

    /**
     * Sets whether the select button is disabled or send an error when the selection is empty or not.
     *
     * @param defaultselectdisabled
     *          {@code true} to disabled the button if no item is disabled,
     *          {@code false} otherwise.
     */
    public void setSelectionDisabledIfEmpty(boolean defaultselectdisabled) {
        getElement().setProperty("defaultselectdisabled", defaultselectdisabled);
    }

    /**
     * Gets whether the select button is disabled or send an error when the selection is empty or not.
     *
     * @return
     *      {@code true} if resizing is enabled,
     *      {@code false} otherwiser (default).
     */
    public boolean getSelectionDisabledIfEmpty() {
        return getElement().getProperty("defaultselectdisabled", true);
    }

    /**
     * Gets the internationalization object previously set for this component.
     * <p>
     * Note: updating the object content that is gotten from this method will
     * not update the lang on the component if not set back using
     * {@link LookupField#setI18n(LookupFieldI18n)}
     *
     * @return the i18n object. It will be <code>null</code>, If the i18n
     *         properties weren't set.
     */
    public LookupFieldI18n getI18n() {
        return i18n;
    }

    /**
     * Sets the internationalization properties for this component.
     *
     * @param i18n
     *            the internationalized properties, not <code>null</code>
     */
    public void setI18n(LookupFieldI18n i18n) {
        Objects.requireNonNull(i18n,
                "The I18N properties object should not be null");
        this.i18n = i18n;
        setI18nWithJS();
    }

    private void setI18nWithJS() {
        runBeforeClientResponse(ui -> {
            JsonObject i18nObject = (JsonObject) JsonSerializer.toJson(i18n);
            for (String key : i18nObject.keys()) {
                getElement().executeJs("this.set('i18n." + key + "', $0)",
                        i18nObject.get(key));
            }
        });
    }

    private void runBeforeClientResponse(SerializableConsumer<UI> command) {
        getElement().getNode().runWhenAttached(ui -> ui
                .beforeClientResponse(this, context -> command.accept(ui)));
    }

    @Override
    public void setValue(T value) {
        comboBox.setValue(value);
    }

    @Override
    public T getValue() {
        return comboBox.getValue();
    }

    @Override
    public Registration addValueChangeListener(ValueChangeListener<? super AbstractField.ComponentValueChangeEvent<LookupField<T>, T>> listener) {
        return comboBox.addValueChangeListener((ValueChangeListener) listener);
    }
    @Override
    public boolean isInvalid() {
        return comboBox.isInvalid();
    }

    @Override
    public void setInvalid(boolean invalid) {
        comboBox.setInvalid(invalid);
    }
    @Override
    public void setErrorMessage(String errorMessage) {
        comboBox.setErrorMessage(errorMessage);
    }
    @Override
    public String getErrorMessage() {
        return comboBox.getErrorMessage();
    }
    @Override
    public String getHelperText() {
        return comboBox.getHelperText();
    }
    @Override
    public void setHelperText(String helperText) {
        comboBox.setHelperText(helperText);
    }
    @Override
    public void setHelperComponent(Component component) {
        comboBox.setHelperComponent(component);
    }

    @Override
    public Component getHelperComponent() {
        return comboBox.getHelperComponent();
    }

    /**
     * Sets the theme variants of this component. This method overwrites any
     * previous set theme variants.
     *
     * @param variants theme variant
     */
    public void setThemeVariants(EnhancedDialogVariant... variants) {
        getElement().getThemeList().clear();
        addThemeVariants(variants);
    }

    /**
     * Adds the theme variants of this component.
     *
     * @param variants theme variant
     */
    public void addThemeVariants(EnhancedDialogVariant... variants) {
        getElement().getThemeList().addAll(Stream.of(variants).map(EnhancedDialogVariant::getVariantName).collect(Collectors.toList()));
    }

    /**
     * Set the header with a custom component
     *
     * @param header custom header
     */
    public void setHeaderComponent(Component header) {
        Objects.requireNonNull(grid, "Header cannot be null");

        if (this.header != null && this.header.getElement().getParent() == getElement()) {
            this.header.getElement().removeFromParent();
        }

        this.header = header;
        header.getElement().setAttribute(SLOT_KEY, HEADER_SLOT_NAME);

        // It might already have a parent e.g when injected from a template
        if (header.getElement().getParent() == null) {
            getElement().appendChild(header.getElement());
        }
    }

    /**
     * Set the footer with a custom component
     * WARNING: You have to implement your own buttons to select and close the dialog
     *
     * @param footer Custom footer
     */
    public void setFooterComponent(Component footer) {
        Objects.requireNonNull(grid, "Footer cannot be null");

        if (this.footer != null && this.footer.getElement().getParent() == getElement()) {
            this.footer.getElement().removeFromParent();
        }

        this.footer = footer;
        footer.getElement().setAttribute(SLOT_KEY, FOOTER_SLOT_NAME);

        // It might already have a parent e.g when injected from a template
        if (footer.getElement().getParent() == null) {
            getElement().appendChild(footer.getElement());
        }
    }

    /**
     * Select and close the dialog
     */
    public void footerSelectAction() {
        copyFieldValueFromGrid();
        footerCloseAction();
    }

    /**
     * Close the dialog
     */
    public void footerCloseAction() {
        getElement().executeJs("$0.__close()", getElement());
    }

    /**
     * Copy the selected value of the field into the grid
     */
    @ClientCallable
    private void openErrorNotification() {
        getNotificationWhenEmptySelection().run();
    }

    private Runnable getNotificationWhenEmptySelection() {
        if (notificationWhenEmptySelection == null) {
            return () -> {
                String emptySelection = (getI18n() == null)? "Please select an item.":getI18n().getEmptyselection();
                new Notification(emptySelection, 2000, Notification.Position.TOP_CENTER).open();
            };
        }
        return notificationWhenEmptySelection;
    }

    /**
     * Replace the default notification to an action
     *
     * @param notificationWhenEmptySelection action to run when the selection is empty and the select button is clicked
     */
    public void addEmptySelectionListener(Runnable notificationWhenEmptySelection) {
        this.notificationWhenEmptySelection = notificationWhenEmptySelection;
    }

    /**
     * The internationalization properties for {@link LookupField}.
     */
    public static class LookupFieldI18n implements Serializable {
        private String select;
        private String cancel;
        private String searcharialabel;
        private String headerprefix;
        private String headerpostfix;
        private String search;
        private String emptyselection;

        public String getSearch() {
            return search;
        }

        public LookupFieldI18n setSearch(String search) {
            this.search = search;
            return this;
        }

        public String getSelect() {
            return select;
        }

        public LookupFieldI18n setSelect(String select) {
            this.select = select;
            return this;
        }

        public String getCancel() {
            return cancel;
        }

        public LookupFieldI18n setCancel(String cancel) {
            this.cancel = cancel;
            return this;
        }

        public String getSearcharialabel() {
            return searcharialabel;
        }

        public LookupFieldI18n setSearcharialabel(String searcharialabel) {
            this.searcharialabel = searcharialabel;
            return this;
        }

        public String getHeaderprefix() {
            return headerprefix;
        }

        public LookupFieldI18n setHeaderprefix(String headerprefix) {
            this.headerprefix = headerprefix;
            return this;
        }

        public String getHeaderpostfix() {
            return headerpostfix;
        }

        public LookupFieldI18n setHeaderpostfix(String headerpostfix) {
            this.headerpostfix = headerpostfix;
            return this;
        }

        public String getEmptyselection() {
            return emptyselection;
        }

        public LookupFieldI18n setEmptyselection(String emptyselection) {
            this.emptyselection = emptyselection;
            return this;
        }
    }
}
