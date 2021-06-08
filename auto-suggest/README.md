# Component Factory AutoSuggest for Vaadin 14+

This is server-side component of [&lt;vcf-auto-suggest&gt;](https://github.com/vaadin-component-factory/vcf-auto-suggest) Web Component.
AutoSuggest is a text input with a panel of suggested options. When user changes the value of the text input a panel with found options will be shown, so that the user can select one of the suggested options.
Lazy loading, default value, customization, component slots, template rendering are all supported features.


[Live Demo ↗](https://incubator.app.fi/auto-suggest-demo/auto-suggest)

## Usage

Some examples of this component usage:

```java
        VerticalLayout col1 = new VerticalLayout(); add(col1);
        
        AutoSuggest<String> autoSuggest1 = new AutoSuggest<>();
        col1.add(new Span("No customization"), autoSuggest1);

        AutoSuggest<String> autoSuggest2 = new AutoSuggest<>();
        autoSuggest2.setShowClearButton(false);
        autoSuggest2.setInputPrefix(new Button("€"));
        autoSuggest2.setPlaceholder("Some placeholder ...");
        autoSuggest2.setItems(generateItems());
        col1.add(new Span("Prefix + no clear button + placeholder"), autoSuggest2);


        AutoSuggest<String> autoSuggest3 = new AutoSuggest<>();
        autoSuggest3.setInputSuffix(new Button("€"));
        autoSuggest3.setItems(generateItems());
        col1.add(new Span("Suffix + clear button"), autoSuggest3);

        AutoSuggest<String> autoSuggest4 = new AutoSuggest<>(true);
        autoSuggest4.setInputSuffix(new Span("€"));
        autoSuggest4.setItems(generateItems());
        col1.add(new Span("Suffix + clear button, clear button placed close to the text"), autoSuggest4);

        AutoSuggest<String> autoSuggest5 = new AutoSuggest<>();
        autoSuggest5.setPlaceholder("Label is up there");
        autoSuggest5.setLabel("This is a label: *");
        autoSuggest5.setItems(generateItems());
        col1.add(new Span("Label (position 1) + placeholder"), autoSuggest5);

        AutoSuggest<String> autoSuggest6 = new AutoSuggest<>();
        autoSuggest6.setItems(generateItems());
        col1.add(new Span("With items (matching mode = STARTS_WITH)"), autoSuggest6);

        AutoSuggest<String> autoSuggest7 = new AutoSuggest<>();
        autoSuggest7.setItems(generateItems());
        autoSuggest7.setSearchMatchingMode(AutoSuggest.SearchMatchingMode.CONTAINS);
        autoSuggest7.setOpenDropdownOnClick(true);
        col1.add(new Span("With items (matching mode = CONTAINS) + openDropdownOnClick"), autoSuggest7);

        AutoSuggest<String> autoSuggest8 = new AutoSuggest<>();
        autoSuggest8.setItems(generateItems());
        autoSuggest8.setOpenDropdownOnClick(true);
        autoSuggest8.setReadOnly(true);
        autoSuggest8.setValue("avocado");
        col1.add(new Span("With items + readonly"), autoSuggest8);

        VerticalLayout col2 = new VerticalLayout(); add(col2);

        AutoSuggest<String> autoSuggest9 = new AutoSuggest<>();
        autoSuggest9.setItems(generateItems());
        autoSuggest9.setOpenDropdownOnClick(true);
        autoSuggest9.setLimit(1);
        col2.add(new Span("Limit = 1"), autoSuggest9);


        Span inputValue10 = new Span("Current input: ");
        Span selectionValue10 = new Span("Selection: ");
        inputValue10.getElement().getStyle().set("font-size", "12px");
        selectionValue10.getElement().getStyle().set("font-size", "12px");
        AutoSuggest<String> autoSuggest10 = new AutoSuggest<>();
        autoSuggest10.setItems(generateItems());

        autoSuggest10.addEagerInputChangeListener(event -> {
            inputValue10.setText("Current input: " + autoSuggest10.getInputValue());
        });

        autoSuggest10.addValueChangeListener(event -> {
            selectionValue10.setText("Selection: " + event.getValue());
        });

        autoSuggest10.addValueClearListener(event -> {
            selectionValue10.setText("Selection: " + "");
        });

        autoSuggest10.setPlaceholder("Search ...");
        autoSuggest10.setThemeName("my-auto-autoSuggest");

        VerticalLayout vl10 = new VerticalLayout(inputValue10, selectionValue10, autoSuggest10);
        vl10.setPadding(false); vl10.setSpacing(false); vl10.setMargin(false);
        col2.add(new Span("Change listeners"), vl10);

        AutoSuggest<String> autoSuggest11 = new AutoSuggest<>();
        autoSuggest11.setItems(generateItems());
        autoSuggest11.setCaseSensitive(true);
        col2.add(new Span("Case sensitive"), autoSuggest11);

        AutoSuggest<String> autoSuggest12 = new AutoSuggest<>();
        autoSuggest12.setLazy(true);
        autoSuggest12.setOpenDropdownOnClick(true);
        autoSuggest12.setComponentToDropdownEndSlot(new HorizontalLayout(new Button("Custom!")));
        col2.add(new Span("Loading + dropdownEndSlot"), autoSuggest12);

        Span inputValue13 = new Span("Current input (lazy) [kw=lazy,avocado]: ");
        Span selectionValue13 = new Span("Selection: ");
        inputValue13.getElement().getStyle().set("font-size", "12px");
        selectionValue13.getElement().getStyle().set("font-size", "12px");
        AutoSuggest<String> autoSuggest13 = new AutoSuggest<>();
        autoSuggest13.setItems(generateItems());
        autoSuggest13.setLazy(true);
        autoSuggest13.addInputChangeListener(event -> { inputValue13.setText("Current input (lazy) [kw=lazy,avocado]: " + autoSuggest13.getInputValue()); });
        autoSuggest13.addInputChangeListener(event -> {
            autoSuggest13.setItems(Arrays.asList(new String[]{ "lazy avocado 1", "lazy avocado 2", "avocado lazy 3" }));
            autoSuggest13.setLoading(false);
        });

        autoSuggest13.addValueChangeListener(event -> {
            selectionValue13.setText("Selection: " + event.getValue());
        });

        autoSuggest13.addValueClearListener(event -> {
            selectionValue13.setText("Selection: " + "");
        });

        autoSuggest13.setPlaceholder("Search ...");
        autoSuggest13.setThemeName("my-auto-autoSuggest");

        VerticalLayout vl13 = new VerticalLayout(inputValue13, selectionValue13, autoSuggest13);
        vl13.setPadding(false); vl13.setSpacing(false); vl13.setMargin(false);
        col2.add(new Span("Lazy"), vl13);

        AutoSuggest<String> autoSuggest14 = new AutoSuggest<>();
        autoSuggest14.setItems(generateItems());
        autoSuggest14.setCustomizeItemsForWhenValueIsNull(true);
        autoSuggest14.setOpenDropdownOnClick(true);
        autoSuggest14.setItemsForWhenValueIsNull(Arrays.asList(new String[] {"different", "list", "this", "one"}));
        col2.add(new Span("Initial autoSuggestions (for when input is empty)"), autoSuggest14);

        AutoSuggest<String> autoSuggest15 = new AutoSuggest<>(5);
        autoSuggest15.setItems(generateItems());
        autoSuggest15.setOpenDropdownOnClick(true);
        autoSuggest15.setComponentToDropdownEndSlot(new HorizontalLayout(new Button("Custom!")));
        autoSuggest15.setDefaultOptionValue("Default!");
        col2.add(new Span("Dropdown end slot + default value"), autoSuggest15);

        AutoSuggest<Fruit> autoSuggest23 = new AutoSuggest<>();
        autoSuggest23.setItems(generateItemsMap());
        autoSuggest23.setOpenDropdownOnClick(true);
        autoSuggest23.setLabelGenerator(item -> item.getName().toLowerCase(Locale.ROOT));
        autoSuggest23.setSearchStringGenerator(item -> "fwc " + item.getName());
        autoSuggest23.setTemplateProvider("function(option, that) { window.handler1 = function(x){console.log(x); that._applyValue(x);}; return `<style>vcf-auto-autoSuggest-overlay vaadin-item {color: blue;}</style><button onclick=\"window.handler1('${option.label}')\" class=\"aaa\">${option.label} ${option.optId}</button>`;}");
        col2.add(new Span("Objects + template provider + customSearch (fwc xxxxx)"), autoSuggest23);

        VerticalLayout col3 = new VerticalLayout(); add(col3);

        AutoSuggest<String> autoSuggest16 = new AutoSuggest<>();
        List<String> items16 = generateItems().stream().collect(Collectors.toList());
        items16.add("This is a very long item and the dropdown should grow to show it");
        autoSuggest16.setItems(items16);
        autoSuggest16.setOpenDropdownOnClick(true);
        col3.add(new Span("Very long option"), autoSuggest16);

        AutoSuggest<String> autoSuggest17 = new AutoSuggest<>();
        autoSuggest17.setItems(generateItems());
        autoSuggest17.setOpenDropdownOnClick(true);
        autoSuggest17.getTextField().setWidth("400px");
        col3.add(new Span("Custom text field width"), autoSuggest17);

        Span inputValue18 = new Span("Current input (lazy) [kw=lazy,avocado], lambda: ");
        Span selectionValue18 = new Span("Selection: ");
        inputValue18.getElement().getStyle().set("font-size", "12px");
        selectionValue18.getElement().getStyle().set("font-size", "12px");
        AutoSuggest<String> autoSuggest18 = new AutoSuggest<>();
        autoSuggest18.setItems(generateItems());
        autoSuggest18.setLazy(true);
        autoSuggest18.setLazyProviderSimple(inputValue -> {
        try { Thread.sleep(4000); } catch (InterruptedException e) { e.printStackTrace(); }
            return Arrays.asList(new String[]{ "lazy avocado 1", "lazy avocado 2", "avocado lazy 3" });
        });
        autoSuggest18.addInputChangeListener(event -> {
            inputValue18.setText("Current input (lazy) [kw=lazy,avocado], lambda: " + autoSuggest18.getInputValue());
        });

        autoSuggest18.addValueChangeListener(event -> {
            selectionValue18.setText("Selection: " + event.getValue());
        });

        autoSuggest18.addValueClearListener(event -> {
            selectionValue18.setText("Selection: " + "");
        });

        autoSuggest18.setPlaceholder("Search ...");
        autoSuggest18.setThemeName("my-auto-suggest");

        VerticalLayout vl18 = new VerticalLayout(inputValue18, selectionValue18, suggest18);
        vl18.setPadding(false); vl18.setSpacing(false); vl18.setMargin(false);
        col3.add(new Span("Lazy, lambda"), vl18);

        AutoSuggest<Fruit> suggest19 = new AutoSuggest<>();
        suggest19.setItems(generateItemsMap());
        suggest19.setOpenDropdownOnClick(true);
        col3.add(new Span("Objects"), suggest19);

        AutoSuggest<Fruit> suggest20 = new AutoSuggest<>();
        suggest20.setItems(generateItemsMap());
        suggest20.setOpenDropdownOnClick(true);
        suggest20.setLabelGenerator(item -> item.getName().toLowerCase(Locale.ROOT));
        col3.add(new Span("Objects + label generator"), suggest20);

        AutoSuggest<Fruit> suggest21 = new AutoSuggest<>();
        suggest21.setItems(generateItemsMap());
        suggest21.setOpenDropdownOnClick(true);
        suggest21.setLabelGenerator(item -> item.getName().toLowerCase(Locale.ROOT));
        suggest21.setSearchStringGenerator(item -> "fwc " + item.getName());
        col3.add(new Span("Objects + label generator + customSearch (fwc xxxxx)"), suggest21);

        AutoSuggest<Fruit> suggest22 = new AutoSuggest<>();
        suggest22.setItems(generateItemsMap());
        suggest22.setOpenDropdownOnClick(true);
        suggest22.setItemsForWhenValueIsNull(generateItems1());
        suggest22.setLabelGenerator(item -> item.getName().toLowerCase(Locale.ROOT));
        suggest22.setItems(generateItems1());
        suggest22.setLazy(true);
        suggest22.setLazyProviderSimple(inputValue -> {
            if(inputValue.trim().length()==0) return Arrays.asList(new Fruit[]{});
            try { Thread.sleep(4000); } catch (InterruptedException e) { e.printStackTrace(); }
            return Arrays.asList(new Fruit[]{ new Fruit("lazy avocado 1"), new Fruit("lazy avocado 2"), new Fruit("avocado lazy 3" )});
        });

        col3.add(new Span("Objects + lazy + different values for input=null"), suggest22);

        AutoSuggest<Fruit> suggest24 = new AutoSuggest<>();
        suggest24.setItems(generateItemsMap());
        suggest24.setOpenDropdownOnClick(true);
        suggest24.setLabelGenerator(item -> item.getName().toLowerCase(Locale.ROOT));
        suggest24.setTemplateProvider("function(option, that) { window.handler1 = function(x){console.log(x); that._applyValue(x);}; return `<style>vcf-auto-suggest-overlay vaadin-item {color: blue;}</style><button onclick=\"window.handler1('${option.label}')\" class=\"aaa\">${option.label}</button>`;}");
        col3.add(new Span("Objects + template provider"), suggest24);

```

## Setting up for development:

Clone the project in GitHub (or fork it if you plan on contributing)

```
git clone git@github.com:vaadin-component-factory/suggest.git
```

to install project, to your maven repository run

```mvn install```


## How to run the demo?

The Demo can be run going to the project `suggest-demo` and executing the maven goal:

```mvn spring-boot:run```


# License & Author

Apache License 2