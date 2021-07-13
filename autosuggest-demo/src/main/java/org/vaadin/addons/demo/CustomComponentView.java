package org.vaadin.addons.demo;

import com.vaadin.flow.component.Text;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.dependency.NpmPackage;
import com.vaadin.flow.component.html.Label;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.FlexLayout;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.ToString;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.theme.Theme;
import com.vaadin.flow.theme.lumo.Lumo;
import org.vaadin.addons.componentfactory.Autosuggest;

import java.io.Serializable;
import java.util.*;
import java.util.stream.Collectors;

@Route(value = "")
@Theme(value = Lumo.class)
//@NpmPackage(value = "@vaadin-component-factory/vcf-autosuggest", version = "1.0.9")
public class CustomComponentView extends VerticalLayout {
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
        setWidthFull();

        Notification notification = new Notification("", 3000);

        FlexLayout first = new FlexLayout(); add(first);
        first.setJustifyContentMode(JustifyContentMode.EVENLY);

        VerticalLayout col1 = new VerticalLayout(); first.add(col1);

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
        autosuggest5.addCustomValueSubmitListener(enterKeyPressEvent -> {
            notification.setText(enterKeyPressEvent.getValue());
            notification.open();
        });
        col1.add(new Span("Label (position 1) + placeholder + custom value event"), autosuggest5);

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
        autosuggest8.setValue("Avocado");
        col1.add(new Span("With items + readonly"), autosuggest8);

        VerticalLayout col2 = new VerticalLayout(); first.add(col2);

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
        autosuggest10.setThemeName("my-autosuggest");

        VerticalLayout vl10 = new VerticalLayout(inputValue10, selectionValue10, autosuggest10);
        vl10.setPadding(false); vl10.setSpacing(false); vl10.setMargin(false);
        col2.add(new Span("Change listeners"), vl10);

        Autosuggest<String> autosuggest11 = new Autosuggest<>();
        autosuggest11.setItems(generateItems());
        autosuggest11.setCaseSensitive(true);
        col2.add(new Span("Case sensitive"), autosuggest11);

        Autosuggest<String> autosuggest12 = new Autosuggest<>();
        //autosuggest12.setItems(generateItems());
        autosuggest12.setLazy(true);
//        autosuggest12.setLoading(true);
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
        autosuggest13.addInputChangeListener(event -> {
            inputValue13.setText("Current input (lazy) [kw=lazy,avocado]: " + autosuggest13.getInputValue());
        });
        autosuggest13.addLazyDataRequestListener(event -> {
            autosuggest13.setItems(Arrays.asList(new String[]{ "lazy avocado 1", "lazy avocado 2", "avocado lazy 3" }));
        });

        autosuggest13.addValueChangeListener(event -> {
            selectionValue13.setText("Selection: " + event.getValue());
        });

        autosuggest13.addValueClearListener(event -> {
            selectionValue13.setText("Selection: " + "");
        });

        autosuggest13.setPlaceholder("Search ...");
        autosuggest13.setThemeName("my-autosuggest");

        VerticalLayout vl13 = new VerticalLayout(inputValue13, selectionValue13, autosuggest13);
        vl13.setPadding(false); vl13.setSpacing(false); vl13.setMargin(false);
        col2.add(new Span("Lazy"), vl13);

        Autosuggest<String> autosuggest14 = new Autosuggest<>();
        autosuggest14.setItems(generateItems());
        autosuggest14.setCustomizeItemsForWhenValueIsNull(true);
        autosuggest14.setOpenDropdownOnClick(true);
        autosuggest14.setItemsForWhenValueIsNull(Arrays.asList(new String[] {"different", "list", "this", "one"}));
        col2.add(new Span("Initial suggestions (for when input is empty)"), autosuggest14);

        Autosuggest<String> autosuggest15 = new Autosuggest<>(5);
        autosuggest15.setItems(generateItems());
        autosuggest15.setOpenDropdownOnClick(true);
        autosuggest15.setComponentToDropdownEndSlot(new HorizontalLayout(new Button("Custom!")));
        //autosuggest15.setDefaultOption("key", "Default!", "Default! + uselessSearchStr");
        autosuggest15.setDefaultOption("", "Default!", "Default! + uselessSearchStr");
        autosuggest15.setSearchMatchingMode(Autosuggest.SearchMatchingMode.CONTAINS);
        col2.add(new Span("Dropdown end slot + default value"), autosuggest15);

        Autosuggest<Fruit> autosuggest23 = new Autosuggest<>();
        autosuggest23.setItems(generateItemsMap());
        autosuggest23.setOpenDropdownOnClick(true);
        autosuggest23.setLabelGenerator(item -> item.getName().toLowerCase(Locale.ROOT));
        autosuggest23.setSearchStringGenerator(item -> "fwc " + item.getName());
        autosuggest23.setTemplateProvider("function(option, that) { window.handler1 = function(x){console.log(x); that._applyValue(x);}; return `<style>vcf-autosuggest-overlay vaadin-item {color: blue;}</style><button onclick=\"window.handler1('${option.key}')\" class=\"aaa\">${option.label} ${option.optId}</button>`;}");
        col2.add(new Span("Objects + template provider + customSearch (fwc xxxxx)"), autosuggest23);

        VerticalLayout col3 = new VerticalLayout(); first.add(col3);

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

        Span inputValue18 = new Span("Current input (lazy) [kw=lazy,avocado], lambda + minimumLengthForLazyReq=3: ");
        Span selectionValue18 = new Span("Selection: ");
        inputValue18.getElement().getStyle().set("font-size", "12px");
        selectionValue18.getElement().getStyle().set("font-size", "12px");
        Autosuggest<String> autosuggest18 = new Autosuggest<>();
        autosuggest18.setItems(generateItems());
        autosuggest18.setMinimumInputLengthToPerformLazyQuery(3);
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

        VerticalLayout vl18 = new VerticalLayout(inputValue18, selectionValue18, autosuggest18);
        vl18.setPadding(false); vl18.setSpacing(false); vl18.setMargin(false);
        col3.add(new Span("Lazy, lambda"), vl18);

        Autosuggest<Fruit> autosuggest19 = new Autosuggest<>();
        autosuggest19.setItems(generateItemsMap());
        autosuggest19.setOpenDropdownOnClick(true);
        col3.add(new Span("Objects"), autosuggest19);

        Autosuggest<Fruit> autosuggest20 = new Autosuggest<>();
        autosuggest20.setItems(generateItemsMap());
        autosuggest20.setOpenDropdownOnClick(true);
        autosuggest20.setLabelGenerator(item -> item.getName().toLowerCase(Locale.ROOT));
        col3.add(new Span("Objects + label generator"), autosuggest20);

        Autosuggest<Fruit> autosuggest21 = new Autosuggest<>();
        autosuggest21.setItems(generateItemsMap());
        autosuggest21.setOpenDropdownOnClick(true);
        autosuggest21.setLabelGenerator(item -> item.getName().toLowerCase(Locale.ROOT));
        autosuggest21.setSearchStringGenerator(item -> "fwc " + item.getName());
        col3.add(new Span("Objects + label generator + customSearch (fwc xxxxx)"), autosuggest21);

        Autosuggest<Fruit> autosuggest22 = new Autosuggest<>();
        autosuggest22.setItems(generateItemsMap());
        autosuggest22.setOpenDropdownOnClick(true);
        autosuggest22.setItemsForWhenValueIsNull(generateItems1());
        autosuggest22.setLabelGenerator(item -> item.getName().toLowerCase(Locale.ROOT));
        autosuggest22.setItems(generateItems1());
        autosuggest22.setLazy(true);
        autosuggest22.setLazyProviderSimple(inputValue -> {
            if(inputValue.trim().length()==0) return Arrays.asList(new Fruit[]{});
            try { Thread.sleep(4000); } catch (InterruptedException e) { e.printStackTrace(); }
            return Arrays.asList(new Fruit[]{ new Fruit("lazy avocado 1"), new Fruit("lazy avocado 2"), new Fruit("avocado lazy 3" )});
        });

        col3.add(new Span("Objects + lazy + different values for input=null"), autosuggest22);

        Autosuggest<Fruit> autosuggest24 = new Autosuggest<>();
        autosuggest24.setItems(generateItemsMap());
        autosuggest24.setOpenDropdownOnClick(true);
        autosuggest24.setLabelGenerator(item -> item.getName().toLowerCase(Locale.ROOT));
        autosuggest24.setTemplateProvider("function(option, that) { window.handler1 = function(x){console.log(x); that._applyValue(x);}; return `<style>vcf-autosuggest-overlay vaadin-item {color: blue;}</style><button onclick=\"window.handler1('${option.key}')\" class=\"aaa\">${option.label}</button>`;}");
        col3.add(new Span("Objects + template provider"), autosuggest24);


        VerticalLayout second = new VerticalLayout(); add(second);
        second.setWidthFull();

        second.add(new Span("TEST EXAMPLE 1 [daniel, john, craig, linda]"));

        Text iTxt = new Text("");
        Text cpTxt = new Text("");

        class Person implements Serializable {
            String name;
            double height;
            String address;

            @Override
            public String toString() {
                return this.name;
            }

            Person(String n, double h, String a) {
                this.name = n;
                this.height = h;
                this.address = a;
            }
        }

        Autosuggest<Person> field = new Autosuggest<>();

        field.setItems(Arrays.asList(new Person[]{
                new Person("daniel", 1.67, "St. John Str"),
                new Person("john", 1.81, "Peace Av."),
                new Person("craig", 1.77, "Uphill Rd."),
                new Person("linda", 1.74, "Central Av."),
        }));

        second.add(new HorizontalLayout(new Label("Input val (triggered by event, retrieved from component): "), iTxt));
        second.add(new HorizontalLayout(new Label("Selection value (triggered by event, retrieved from component): "), cpTxt));
        second.add(field);

        field.addEagerInputChangeListener(e -> {
            int x = 35;
        });

        field.addInputChangeListener(e -> {
            iTxt.setText(field.getInputValue());
        });

        field.addValueChangeListener(e -> {
            Person x = field.getValue();
            cpTxt.setText(x == null ? "" : x.address);
        });

        class PersonH extends Person implements Serializable {
            @Override
            public String toString() {
                return String.format("%d", this.hashCode());
            }

            PersonH(String n, double h, String a) {
                super(n, h, a);
            }

            public String getName() {
                return this.name;
            }
        }

        second.add(new Span("TEST EXAMPLE 2 [john, john, john, john]"));
        Text cpTxt2 = new Text("");
        Autosuggest<PersonH> field2 = new Autosuggest<>();
        field2.setItems(Arrays.asList(new PersonH[]{
                new PersonH("john", 1.67, "St. John Str"),
                new PersonH("john", 1.81, "Peace Av."),
                new PersonH("john", 1.77, "Uphill Rd."),
                new PersonH("john", 1.74, "Central Av."),
        }));
        field2.addValueChangeListener(e -> {
            Person x = field2.getValue();
            cpTxt2.setText(x == null ? "" : x.address);
        });
        field2.setLabelGenerator(PersonH::getName);
        field2.setSearchStringGenerator(item -> item.getName().toLowerCase(Locale.ROOT));
        field2.setOpenDropdownOnClick(true);

        second.add(new HorizontalLayout(new Label("Selection value (triggered by event, retrieved from component): "), cpTxt2));
        second.add(field2);
    }

}
