/**
 * @license
 * Copyright (C) 2015 Vaadin Ltd.
 * This program is available under Commercial Vaadin Add-On License 3.0 (CVALv3).
 * See the file LICENSE.md distributed with this software for more information about licensing.
 * See [the website]{@link https://vaadin.com/license/cval-3} for the complete license.
 */

 import { registerStyles, css } from '@vaadin/vaadin-themable-mixin/register-styles.js';
 import { OverlayElement } from '@vaadin/vaadin-overlay/src/vaadin-overlay';

 registerStyles(
   'vcf-autosuggest-overlay',
   css`
     :host {
       align-items: flex-start;
       justify-content: flex-start;
       right: auto;
       position: absolute;
       bottom: auto;
       background: #fff;
     }

     [part='content'] {
         padding: 0;
     }

     [part='overlay'] {
       background-color: var(--lumo-base-color);
       background-image: linear-gradient(var(--lumo-tint-5pct), var(--lumo-tint-5pct));
       border-radius: var(--lumo-border-radius);
       box-shadow: 0 0 0 1px var(--lumo-shade-5pct), var(--lumo-box-shadow-m);
     }
   `
 );

 /**
  * `<vcf-autosuggest-overlay>` The autosuggest overlay element.
  *
  * ```html
  * <vcf-autosuggest-overlay></vcf-autosuggest-overlay>
  * ```
  *
  * ### Styling
  *
  * See [`<vaadin-overlay>` documentation](https://github.com/vaadin/vaadin-overlay/blob/master/src/vaadin-overlay.html)
  * for `<vaadin-dropdown-menu-overlay>` parts.
  *
  * See [ThemableMixin – how to apply styles for shadow parts](https://github.com/vaadin/vaadin-themable-mixin/wiki)
  *
  * @polymer
  * @extends OverlayElement
  * @demo demo/index.html
  */
 class AutosuggestOverlayElement extends OverlayElement {
     static get is() {
         return 'vcf-autosuggest-overlay';
     }
 }
 customElements.define(AutosuggestOverlayElement.is, AutosuggestOverlayElement);
