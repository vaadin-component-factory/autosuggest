# Component Factory Autosuggest for Vaadin 14+

This is server-side component of [&lt;vcf-autosuggest&gt;](https://github.com/vaadin-component-factory/vcf-autosuggest) Web Component.
Autosuggest is a text input with a panel of suggested options. When user changes the value of the text input a panel with found options will be shown, so that the user can select one of the suggested options.
Lazy loading, default value, customization, component slots, template rendering are all supported features.


[Live Demo ↗](https://incubator.app.fi/autosuggest-demo)

## Usage

Some examples of this component usage:

```java
        VerticalLayout col1 = new VerticalLayout(); add(col1);
        
        Autosuggest<String> autosuggest1 = new Autosuggest<>();
        col1.add(new Span("No customization"), autosuggest1);

        Autosuggest<String> autosuggest2 = new Autosuggest<>();
        autosuggest2.setShowClearButton(false);
        autosuggest2.setInputPrefix(new Button("€"));
        autosuggest2.setPlaceholder("Some placeholder ...");
        autosuggest2.setItems(generateItems());
        col1.add(new Span("Prefix + no clear button + placeholder"), autosuggest2);


        Autosuggest<String> autosuggest3 = new Autosuggest<>();
        autosuggest3.setInputSuffix(new Button("€"));
        autosuggest3.setItems(generateItems());
        col1.add(new Span("Suffix + clear button"), autosuggest3);

        Autosuggest<String> autosuggest4 = new Autosuggest<>(true);
        autosuggest4.setInputSuffix(new Span("€"));
        autosuggest4.setItems(generateItems());
        col1.add(new Span("Suffix + clear button, clear button placed close to the text"), autosuggest4);

        Autosuggest<String> autosuggest5 = new Autosuggest<>();
        autosuggest5.setPlaceholder("Label is up there");
        autosuggest5.setLabel("This is a label: *");
        autosuggest5.setItems(generateItems());
        col1.add(new Span("Label (position 1) + placeholder"), autosuggest5);

        Autosuggest<String> autosuggest6 = new Autosuggest<>();
        autosuggest6.setItems(generateItems());
        col1.add(new Span("With items (matching mode = STARTS_WITH)"), autosuggest6);

        Autosuggest<String> autosuggest7 = new Autosuggest<>();
        autosuggest7.setItems(generateItems());
        autosuggest7.setSearchMatchingMode(Autosuggest.SearchMatchingMode.CONTAINS);
        autosuggest7.setOpenDropdownOnClick(true);
        col1.add(new Span("With items (matching mode = CONTAINS) + openDropdownOnClick"), autosuggest7);

        Autosuggest<String> autosuggest8 = new Autosuggest<>();
        autosuggest8.setItems(generateItems());
        autosuggest8.setOpenDropdownOnClick(true);
        autosuggest8.setReadOnly(true);
        autosuggest8.setValue("avocado");
        col1.add(new Span("With items + readonly"), autosuggest8);

        VerticalLayout col2 = new VerticalLayout(); add(col2);

        Autosuggest<String> autosuggest9 = new Autosuggest<>();
        autosuggest9.setItems(generateItems());
        autosuggest9.setOpenDropdownOnClick(true);
        autosuggest9.setLimit(1);
        col2.add(new Span("Limit = 1"), autosuggest9);


        Span inputValue10 = new Span("Current input: ");
        Span selectionValue10 = new Span("Selection: ");
        inputValue10.getElement().getStyle().set("font-size", "12px");
        selectionValue10.getElement().getStyle().set("font-size", "12px");
        Autosuggest<String> autosuggest10 = new Autosuggest<>();
        autosuggest10.setItems(generateItems());

        autosuggest10.addEagerInputChangeListener(event -> {
            inputValue10.setText("Current input: " + autosuggest10.getInputValue());
        });

        autosuggest10.addValueChangeListener(event -> {
            selectionValue10.setText("Selection: " + event.getValue());
        });

        autosuggest10.addValueClearListener(event -> {
            selectionValue10.setText("Selection: " + "");
        });

        autosuggest10.setPlaceholder("Search ...");
        autosuggest10.setThemeName("my-auto-autosuggest");

        VerticalLayout vl10 = new VerticalLayout(inputValue10, selectionValue10, autosuggest10);
        vl10.setPadding(false); vl10.setSpacing(false); vl10.setMargin(false);
        col2.add(new Span("Change listeners"), vl10);

        Autosuggest<String> autosuggest11 = new Autosuggest<>();
        autosuggest11.setItems(generateItems());
        autosuggest11.setCaseSensitive(true);
        col2.add(new Span("Case sensitive"), autosuggest11);

        Autosuggest<String> autosuggest12 = new Autosuggest<>();
        autosuggest12.setLazy(true);
        autosuggest12.setOpenDropdownOnClick(true);
        autosuggest12.setComponentToDropdownEndSlot(new HorizontalLayout(new Button("Custom!")));
        col2.add(new Span("Loading + dropdownEndSlot"), autosuggest12);

        Span inputValue13 = new Span("Current input (lazy) [kw=lazy,avocado]: ");
        Span selectionValue13 = new Span("Selection: ");
        inputValue13.getElement().getStyle().set("font-size", "12px");
        selectionValue13.getElement().getStyle().set("font-size", "12px");
        Autosuggest<String> autosuggest13 = new Autosuggest<>();
        autosuggest13.setItems(generateItems());
        autosuggest13.setLazy(true);
        autosuggest13.addInputChangeListener(event -> { inputValue13.setText("Current input (lazy) [kw=lazy,avocado]: " + autosuggest13.getInputValue()); });
        autosuggest13.addInputChangeListener(event -> {
            autosuggest13.setItems(Arrays.asList(new String[]{ "lazy avocado 1", "lazy avocado 2", "avocado lazy 3" }));
            autosuggest13.setLoading(false);
        });

        autosuggest13.addValueChangeListener(event -> {
            selectionValue13.setText("Selection: " + event.getValue());
        });

        autosuggest13.addValueClearListener(event -> {
            selectionValue13.setText("Selection: " + "");
        });

        autosuggest13.setPlaceholder("Search ...");
        autosuggest13.setThemeName("my-auto-autosuggest");

        VerticalLayout vl13 = new VerticalLayout(inputValue13, selectionValue13, autosuggest13);
        vl13.setPadding(false); vl13.setSpacing(false); vl13.setMargin(false);
        col2.add(new Span("Lazy"), vl13);

        Autosuggest<String> autosuggest14 = new Autosuggest<>();
        autosuggest14.setItems(generateItems());
        autosuggest14.setCustomizeItemsForWhenValueIsNull(true);
        autosuggest14.setOpenDropdownOnClick(true);
        autosuggest14.setItemsForWhenValueIsNull(Arrays.asList(new String[] {"different", "list", "this", "one"}));
        col2.add(new Span("Initial autosuggestions (for when input is empty)"), autosuggest14);

        Autosuggest<String> autosuggest15 = new Autosuggest<>(5);
        autosuggest15.setItems(generateItems());
        autosuggest15.setOpenDropdownOnClick(true);
        autosuggest15.setComponentToDropdownEndSlot(new HorizontalLayout(new Button("Custom!")));
        autosuggest15.setDefaultOptionValue("Default!");
        col2.add(new Span("Dropdown end slot + default value"), autosuggest15);

        Autosuggest<Fruit> autosuggest23 = new Autosuggest<>();
        autosuggest23.setItems(generateItemsMap());
        autosuggest23.setOpenDropdownOnClick(true);
        autosuggest23.setLabelGenerator(item -> item.getName().toLowerCase(Locale.ROOT));
        autosuggest23.setSearchStringGenerator(item -> "fwc " + item.getName());
        autosuggest23.setTemplateProvider("function(option, that) { window.handler1 = function(x){console.log(x); that._applyValue(x);}; return `<style>vcf-auto-autosuggest-overlay vaadin-item {color: blue;}</style><button onclick=\"window.handler1('${option.label}')\" class=\"aaa\">${option.label} ${option.optId}</button>`;}");
        col2.add(new Span("Objects + template provider + customSearch (fwc xxxxx)"), autosuggest23);

        VerticalLayout col3 = new VerticalLayout(); add(col3);

        Autosuggest<String> autosuggest16 = new Autosuggest<>();
        List<String> items16 = generateItems().stream().collect(Collectors.toList());
        items16.add("This is a very long item and the dropdown should grow to show it");
        autosuggest16.setItems(items16);
        autosuggest16.setOpenDropdownOnClick(true);
        col3.add(new Span("Very long option"), autosuggest16);

        Autosuggest<String> autosuggest17 = new Autosuggest<>();
        autosuggest17.setItems(generateItems());
        autosuggest17.setOpenDropdownOnClick(true);
        autosuggest17.getTextField().setWidth("400px");
        col3.add(new Span("Custom text field width"), autosuggest17);

        Span inputValue18 = new Span("Current input (lazy) [kw=lazy,avocado], lambda: ");
        Span selectionValue18 = new Span("Selection: ");
        inputValue18.getElement().getStyle().set("font-size", "12px");
        selectionValue18.getElement().getStyle().set("font-size", "12px");
        Autosuggest<String> autosuggest18 = new Autosuggest<>();
        autosuggest18.setItems(generateItems());
        autosuggest18.setLazy(true);
        autosuggest18.setLazyProviderSimple(inputValue -> {
        try { Thread.sleep(4000); } catch (InterruptedException e) { e.printStackTrace(); }
            return Arrays.asList(new String[]{ "lazy avocado 1", "lazy avocado 2", "avocado lazy 3" });
        });
        autosuggest18.addInputChangeListener(event -> {
            inputValue18.setText("Current input (lazy) [kw=lazy,avocado], lambda: " + autosuggest18.getInputValue());
        });

        autosuggest18.addValueChangeListener(event -> {
            selectionValue18.setText("Selection: " + event.getValue());
        });

        autosuggest18.addValueClearListener(event -> {
            selectionValue18.setText("Selection: " + "");
        });

        autosuggest18.setPlaceholder("Search ...");
        autosuggest18.setThemeName("my-autosuggest");

        VerticalLayout vl18 = new VerticalLayout(inputValue18, selectionValue18, suggest18);
        vl18.setPadding(false); vl18.setSpacing(false); vl18.setMargin(false);
        col3.add(new Span("Lazy, lambda"), vl18);

        Autosuggest<Fruit> suggest19 = new Autosuggest<>();
        suggest19.setItems(generateItemsMap());
        suggest19.setOpenDropdownOnClick(true);
        col3.add(new Span("Objects"), suggest19);

        Autosuggest<Fruit> suggest20 = new Autosuggest<>();
        suggest20.setItems(generateItemsMap());
        suggest20.setOpenDropdownOnClick(true);
        suggest20.setLabelGenerator(item -> item.getName().toLowerCase(Locale.ROOT));
        col3.add(new Span("Objects + label generator"), suggest20);

        Autosuggest<Fruit> suggest21 = new Autosuggest<>();
        suggest21.setItems(generateItemsMap());
        suggest21.setOpenDropdownOnClick(true);
        suggest21.setLabelGenerator(item -> item.getName().toLowerCase(Locale.ROOT));
        suggest21.setSearchStringGenerator(item -> "fwc " + item.getName());
        col3.add(new Span("Objects + label generator + customSearch (fwc xxxxx)"), suggest21);

        Autosuggest<Fruit> suggest22 = new Autosuggest<>();
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

        Autosuggest<Fruit> suggest24 = new Autosuggest<>();
        suggest24.setItems(generateItemsMap());
        suggest24.setOpenDropdownOnClick(true);
        suggest24.setLabelGenerator(item -> item.getName().toLowerCase(Locale.ROOT));
        suggest24.setTemplateProvider("function(option, that) { window.handler1 = function(x){console.log(x); that._applyValue(x);}; return `<style>vcf-autosuggest-overlay vaadin-item {color: blue;}</style><button onclick=\"window.handler1('${option.label}')\" class=\"aaa\">${option.label}</button>`;}");
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