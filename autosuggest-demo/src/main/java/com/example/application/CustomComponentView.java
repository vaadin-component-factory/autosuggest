package com.example.application;

import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.orderedlayout.FlexLayout;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.ToString;
import org.vaadin.componentfactory.EnhancedAutocomplete;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.theme.Theme;
import com.vaadin.flow.theme.lumo.Lumo;

import java.util.*;
import java.util.stream.Collectors;

@Route(value = "")
@Theme(value = Lumo.class)
public class CustomComponentView extends FlexLayout {
    @Data
    @AllArgsConstructor
    @ToString
    class Fruit {
        String name;
    }

    public List<String> generateItems() {
        return Arrays.asList(new String[] {"Avocado", "Banana", "Tomato", "Cherry", "Orange", "Passionfruit", "Startfruit", "Strawberry", "Apple", "Pineapple", "Pear"});
    }

    public List<Fruit> generateItems1() {
        List<Fruit> fruits = new ArrayList<>();
        fruits.add(new Fruit("BANANA"));
        fruits.add(new Fruit("PEACH"));
        return fruits;
    }

    public Map<String, Fruit> generateItemsMap() {
        Map<String, Fruit> res = new HashMap<>();
        Arrays.asList(new String[] {"Avocado", "Banana", "Tomato", "Cherry", "Orange", "Passionfruit", "Startfruit", "Strawberry", "Apple", "Pineapple", "Pear"}).stream()
                .forEach(item -> res.put(item + " F", new Fruit(item)));
        return res;
    }

    public CustomComponentView() {
        setJustifyContentMode(JustifyContentMode.EVENLY);

        VerticalLayout col1 = new VerticalLayout(); add(col1);

        EnhancedAutocomplete<String> autocomplete1 = new EnhancedAutocomplete<>();
        col1.add(new Span("No customization"), autocomplete1);

        EnhancedAutocomplete<String> autocomplete2 = new EnhancedAutocomplete<>();
        autocomplete2.setShowClearButton(false);
        autocomplete2.setInputPrefix(new Button("€"));
        autocomplete2.setPlaceholder("Some placeholder ...");
        autocomplete2.setItems(generateItems());
        col1.add(new Span("Prefix + no clear button + placeholder"), autocomplete2);


        EnhancedAutocomplete<String> autocomplete3 = new EnhancedAutocomplete<>();
        autocomplete3.setInputSuffix(new Button("€"));
        autocomplete3.setItems(generateItems());
        col1.add(new Span("Suffix + clear button"), autocomplete3);

        EnhancedAutocomplete<String> autocomplete4 = new EnhancedAutocomplete<>(true);
        autocomplete4.setInputSuffix(new Span("€"));
        autocomplete4.setItems(generateItems());
        col1.add(new Span("Suffix + clear button, clear button placed close to the text"), autocomplete4);

        EnhancedAutocomplete<String> autocomplete5 = new EnhancedAutocomplete<>();
        autocomplete5.setPlaceholder("Label is up there");
        autocomplete5.setLabel("This is a label: *");
        autocomplete5.setItems(generateItems());
        col1.add(new Span("Label (position 1) + placeholder"), autocomplete5);

        EnhancedAutocomplete<String> autocomplete6 = new EnhancedAutocomplete<>();
        autocomplete6.setItems(generateItems());
        col1.add(new Span("With items (matching mode = STARTS_WITH)"), autocomplete6);

        EnhancedAutocomplete<String> autocomplete7 = new EnhancedAutocomplete<>();
        autocomplete7.setItems(generateItems());
        autocomplete7.setSearchMatchingMode(EnhancedAutocomplete.SearchMatchingMode.CONTAINS);
        autocomplete7.setOpenDropdownOnClick(true);
        col1.add(new Span("With items (matching mode = CONTAINS) + openDropdownOnClick"), autocomplete7);

        EnhancedAutocomplete<String> autocomplete8 = new EnhancedAutocomplete<>();
        autocomplete8.setItems(generateItems());
        autocomplete8.setOpenDropdownOnClick(true);
        autocomplete8.setReadOnly(true);
        autocomplete8.setValue("avocado");
        col1.add(new Span("With items + readonly"), autocomplete8);

        VerticalLayout col2 = new VerticalLayout(); add(col2);

        EnhancedAutocomplete<String> autocomplete9 = new EnhancedAutocomplete<>();
        autocomplete9.setItems(generateItems());
        autocomplete9.setOpenDropdownOnClick(true);
        autocomplete9.setLimit(1);
        col2.add(new Span("Limit = 1"), autocomplete9);


        Span inputValue10 = new Span("Current input: ");
        Span selectionValue10 = new Span("Selection: ");
        inputValue10.getElement().getStyle().set("font-size", "12px");
        selectionValue10.getElement().getStyle().set("font-size", "12px");
        EnhancedAutocomplete<String> autocomplete10 = new EnhancedAutocomplete<>();
        autocomplete10.setItems(generateItems());

        autocomplete10.addEagerInputChangeListener(event -> {
            inputValue10.setText("Current input: " + autocomplete10.getInputValue());
        });

        autocomplete10.addValueChangeListener(event -> {
            selectionValue10.setText("Selection: " + event.getValue());
        });

        autocomplete10.addValueClearListener(event -> {
            selectionValue10.setText("Selection: " + "");
        });

        autocomplete10.setPlaceholder("Search ...");
        autocomplete10.setThemeName("my-autocomplete");

        VerticalLayout vl10 = new VerticalLayout(inputValue10, selectionValue10, autocomplete10);
        vl10.setPadding(false); vl10.setSpacing(false); vl10.setMargin(false);
        col2.add(new Span("Change listeners"), vl10);

        EnhancedAutocomplete<String> autocomplete11 = new EnhancedAutocomplete<>();
        autocomplete11.setItems(generateItems());
        autocomplete11.setCaseSensitive(true);
        col2.add(new Span("Case sensitive"), autocomplete11);

        EnhancedAutocomplete<String> autocomplete12 = new EnhancedAutocomplete<>();
        //autocomplete12.setItems(generateItems());
        autocomplete12.setLazy(true);
//        autocomplete12.setLoading(true);
        autocomplete12.setOpenDropdownOnClick(true);
        autocomplete12.setComponentToDropdownEndSlot(new HorizontalLayout(new Button("Custom!")));
        col2.add(new Span("Loading + dropdownEndSlot"), autocomplete12);

        Span inputValue13 = new Span("Current input (lazy) [kw=lazy,avocado]: ");
        Span selectionValue13 = new Span("Selection: ");
        inputValue13.getElement().getStyle().set("font-size", "12px");
        selectionValue13.getElement().getStyle().set("font-size", "12px");
        EnhancedAutocomplete<String> autocomplete13 = new EnhancedAutocomplete<>();
        autocomplete13.setItems(generateItems());
        autocomplete13.setLazy(true);
        autocomplete13.addInputChangeListener(event -> { inputValue13.setText("Current input (lazy) [kw=lazy,avocado]: " + autocomplete13.getInputValue()); });
        autocomplete13.addInputChangeListener(event -> {
            autocomplete13.setItems(Arrays.asList(new String[]{ "lazy avocado 1", "lazy avocado 2", "avocado lazy 3" }));
            autocomplete13.setLoading(false);
        });

        autocomplete13.addValueChangeListener(event -> {
            selectionValue13.setText("Selection: " + event.getValue());
        });

        autocomplete13.addValueClearListener(event -> {
            selectionValue13.setText("Selection: " + "");
        });

        autocomplete13.setPlaceholder("Search ...");
        autocomplete13.setThemeName("my-autocomplete");

        VerticalLayout vl13 = new VerticalLayout(inputValue13, selectionValue13, autocomplete13);
        vl13.setPadding(false); vl13.setSpacing(false); vl13.setMargin(false);
        col2.add(new Span("Lazy"), vl13);

        EnhancedAutocomplete<String> autocomplete14 = new EnhancedAutocomplete<>();
        autocomplete14.setItems(generateItems());
        autocomplete14.setCustomizeItemsForWhenValueIsNull(true);
        autocomplete14.setOpenDropdownOnClick(true);
        autocomplete14.setItemsForWhenValueIsNull(Arrays.asList(new String[] {"different", "list", "this", "one"}));
        col2.add(new Span("Initial suggestions (for when input is empty)"), autocomplete14);

        EnhancedAutocomplete<String> autocomplete15 = new EnhancedAutocomplete<>(5);
        autocomplete15.setItems(generateItems());
        autocomplete15.setOpenDropdownOnClick(true);
        autocomplete15.setComponentToDropdownEndSlot(new HorizontalLayout(new Button("Custom!")));
        autocomplete15.setDefaultOptionValue("Default!");
        col2.add(new Span("Dropdown end slot + default value"), autocomplete15);

        EnhancedAutocomplete<Fruit> autocomplete23 = new EnhancedAutocomplete<>();
        autocomplete23.setItems(generateItemsMap());
        autocomplete23.setOpenDropdownOnClick(true);
        autocomplete23.setLabelGenerator(item -> item.getName().toLowerCase(Locale.ROOT));
        autocomplete23.setSearchStringGenerator(item -> "fwc " + item.getName());
        autocomplete23.setTemplateProvider("function(option, that) { window.handler1 = function(x){console.log(x); that._applyValue(x);}; return `<style>vcf-enhanced-autocomplete-overlay vaadin-item {color: blue;}</style><button onclick=\"window.handler1('${option.label}')\" class=\"aaa\">${option.label} ${option.optId}</button>`;}");
        col2.add(new Span("Objects + template provider + customSearch (fwc xxxxx)"), autocomplete23);

        VerticalLayout col3 = new VerticalLayout(); add(col3);

        EnhancedAutocomplete<String> autocomplete16 = new EnhancedAutocomplete<>();
        List<String> items16 = generateItems().stream().collect(Collectors.toList());
        items16.add("This is a very long item and the dropdown should grow to show it");
        autocomplete16.setItems(items16);
        autocomplete16.setOpenDropdownOnClick(true);
        col3.add(new Span("Very long option"), autocomplete16);

        EnhancedAutocomplete<String> autocomplete17 = new EnhancedAutocomplete<>();
        autocomplete17.setItems(generateItems());
        autocomplete17.setOpenDropdownOnClick(true);
        autocomplete17.getTextField().setWidth("400px");
        col3.add(new Span("Custom text field width"), autocomplete17);

        Span inputValue18 = new Span("Current input (lazy) [kw=lazy,avocado], lambda: ");
        Span selectionValue18 = new Span("Selection: ");
        inputValue18.getElement().getStyle().set("font-size", "12px");
        selectionValue18.getElement().getStyle().set("font-size", "12px");
        EnhancedAutocomplete<String> autocomplete18 = new EnhancedAutocomplete<>();
        autocomplete18.setItems(generateItems());
        autocomplete18.setLazy(true);
        autocomplete18.setLazyProviderSimple(inputValue -> {
            try { Thread.sleep(4000); } catch (InterruptedException e) { e.printStackTrace(); }
            return Arrays.asList(new String[]{ "lazy avocado 1", "lazy avocado 2", "avocado lazy 3" });
        });
        autocomplete18.addInputChangeListener(event -> {
            inputValue18.setText("Current input (lazy) [kw=lazy,avocado], lambda: " + autocomplete18.getInputValue());
        });

        autocomplete18.addValueChangeListener(event -> {
            selectionValue18.setText("Selection: " + event.getValue());
        });

        autocomplete18.addValueClearListener(event -> {
            selectionValue18.setText("Selection: " + "");
        });

        autocomplete18.setPlaceholder("Search ...");
        autocomplete18.setThemeName("my-autocomplete");

        VerticalLayout vl18 = new VerticalLayout(inputValue18, selectionValue18, autocomplete18);
        vl18.setPadding(false); vl18.setSpacing(false); vl18.setMargin(false);
        col3.add(new Span("Lazy, lambda"), vl18);

        EnhancedAutocomplete<Fruit> autocomplete19 = new EnhancedAutocomplete<>();
        autocomplete19.setItems(generateItemsMap());
        autocomplete19.setOpenDropdownOnClick(true);
        col3.add(new Span("Objects"), autocomplete19);

        EnhancedAutocomplete<Fruit> autocomplete20 = new EnhancedAutocomplete<>();
        autocomplete20.setItems(generateItemsMap());
        autocomplete20.setOpenDropdownOnClick(true);
        autocomplete20.setLabelGenerator(item -> item.getName().toLowerCase(Locale.ROOT));
        col3.add(new Span("Objects + label generator"), autocomplete20);

        EnhancedAutocomplete<Fruit> autocomplete21 = new EnhancedAutocomplete<>();
        autocomplete21.setItems(generateItemsMap());
        autocomplete21.setOpenDropdownOnClick(true);
        autocomplete21.setLabelGenerator(item -> item.getName().toLowerCase(Locale.ROOT));
        autocomplete21.setSearchStringGenerator(item -> "fwc " + item.getName());
        col3.add(new Span("Objects + label generator + customSearch (fwc xxxxx)"), autocomplete21);

        EnhancedAutocomplete<Fruit> autocomplete22 = new EnhancedAutocomplete<>();
        autocomplete22.setItems(generateItemsMap());
        autocomplete22.setOpenDropdownOnClick(true);
        autocomplete22.setItemsForWhenValueIsNull(generateItems1());
        autocomplete22.setLabelGenerator(item -> item.getName().toLowerCase(Locale.ROOT));
        autocomplete22.setItems(generateItems1());
        autocomplete22.setLazy(true);
        autocomplete22.setLazyProviderSimple(inputValue -> {
            if(inputValue.trim().length()==0) return Arrays.asList(new Fruit[]{});
            try { Thread.sleep(4000); } catch (InterruptedException e) { e.printStackTrace(); }
            return Arrays.asList(new Fruit[]{ new Fruit("lazy avocado 1"), new Fruit("lazy avocado 2"), new Fruit("avocado lazy 3" )});
        });

        col3.add(new Span("Objects + lazy + different values for input=null"), autocomplete22);

        EnhancedAutocomplete<Fruit> autocomplete24 = new EnhancedAutocomplete<>();
        autocomplete24.setItems(generateItemsMap());
        autocomplete24.setOpenDropdownOnClick(true);
        autocomplete24.setLabelGenerator(item -> item.getName().toLowerCase(Locale.ROOT));
        autocomplete24.setTemplateProvider("function(option, that) { window.handler1 = function(x){console.log(x); that._applyValue(x);}; return `<style>vcf-enhanced-autocomplete-overlay vaadin-item {color: blue;}</style><button onclick=\"window.handler1('${option.label}')\" class=\"aaa\">${option.label}</button>`;}");
        col3.add(new Span("Objects + template provider"), autocomplete24);
    }

}
