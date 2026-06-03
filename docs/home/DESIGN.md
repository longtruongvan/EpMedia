---
name: Lumina Edit
colors:
  surface: '#131314'
  surface-dim: '#131314'
  surface-bright: '#39393a'
  surface-container-lowest: '#0e0e0f'
  surface-container-low: '#1c1b1c'
  surface-container: '#201f20'
  surface-container-high: '#2a2a2b'
  surface-container-highest: '#353436'
  on-surface: '#e5e2e3'
  on-surface-variant: '#bcc8d1'
  inverse-surface: '#e5e2e3'
  inverse-on-surface: '#313031'
  outline: '#86929a'
  outline-variant: '#3d484f'
  surface-tint: '#75d1ff'
  primary: '#92d9ff'
  on-primary: '#003548'
  primary-container: '#00c2ff'
  on-primary-container: '#004c66'
  inverse-primary: '#006688'
  secondary: '#d1bcff'
  on-secondary: '#3c0090'
  secondary-container: '#7000ff'
  on-secondary-container: '#ddcdff'
  tertiary: '#ffc1b9'
  on-tertiary: '#690003'
  tertiary-container: '#ff998c'
  on-tertiary-container: '#920005'
  error: '#ffb4ab'
  on-error: '#690005'
  error-container: '#93000a'
  on-error-container: '#ffdad6'
  primary-fixed: '#c2e8ff'
  primary-fixed-dim: '#75d1ff'
  on-primary-fixed: '#001e2b'
  on-primary-fixed-variant: '#004d67'
  secondary-fixed: '#e9ddff'
  secondary-fixed-dim: '#d1bcff'
  on-secondary-fixed: '#23005b'
  on-secondary-fixed-variant: '#5700c9'
  tertiary-fixed: '#ffdad5'
  tertiary-fixed-dim: '#ffb4aa'
  on-tertiary-fixed: '#410001'
  on-tertiary-fixed-variant: '#930005'
  background: '#131314'
  on-background: '#e5e2e3'
  surface-variant: '#353436'
typography:
  display:
    fontFamily: Hanken Grotesk
    fontSize: 34px
    fontWeight: '700'
    lineHeight: 41px
    letterSpacing: -0.02em
  headline-lg:
    fontFamily: Hanken Grotesk
    fontSize: 24px
    fontWeight: '600'
    lineHeight: 30px
    letterSpacing: -0.01em
  headline-md:
    fontFamily: Hanken Grotesk
    fontSize: 20px
    fontWeight: '600'
    lineHeight: 25px
  body-lg:
    fontFamily: Inter
    fontSize: 17px
    fontWeight: '400'
    lineHeight: 24px
  body-md:
    fontFamily: Inter
    fontSize: 15px
    fontWeight: '400'
    lineHeight: 20px
  label-lg:
    fontFamily: JetBrains Mono
    fontSize: 13px
    fontWeight: '500'
    lineHeight: 16px
    letterSpacing: 0.02em
  label-sm:
    fontFamily: JetBrains Mono
    fontSize: 11px
    fontWeight: '500'
    lineHeight: 14px
    letterSpacing: 0.05em
  headline-lg-mobile:
    fontFamily: Hanken Grotesk
    fontSize: 22px
    fontWeight: '600'
    lineHeight: 28px
rounded:
  sm: 0.25rem
  DEFAULT: 0.5rem
  md: 0.75rem
  lg: 1rem
  xl: 1.5rem
  full: 9999px
spacing:
  unit: 4px
  touch-target-min: 44px
  margin-edge: 16px
  gutter: 12px
  stack-sm: 8px
  stack-md: 16px
  stack-lg: 24px
---

## Brand & Style

This design system is engineered for a premium, creator-centric mobile video editing experience. The brand personality is professional, precise, and unobtrusive, ensuring the user's content remains the focal point. 

The aesthetic blends **Modern Corporate** reliability with **Glassmorphic** depth. It leverages the structured logic of Material Design 3 and the refined ergonomics of iOS. The UI evokes a sense of high-end hardware, utilizing deep blacks, vibrant accents, and smooth transitions to simulate a professional studio environment on a handheld device.

## Colors

The palette is anchored by a deep "True Black" background (`#0F0F10`) to maximize OLED contrast and minimize bezel distraction. 

- **Primary:** A high-vibrancy Electric Cyan used for active states, playback heads, and primary actions.
- **Secondary:** A deep Violet for creative tools and multi-track differentiation.
- **Error/Destructive:** A vivid Red for delete actions and warnings.
- **Neutral/Surface:** Systematic grays layered over the background to create a clear information hierarchy. Surfaces use subtle transparency to maintain a sense of depth.

## Typography

The typography strategy prioritizes legibility in a dark, high-density environment. 

- **Headlines:** Uses **Hanken Grotesk** for a sharp, contemporary feel that signals precision.
- **Body:** Uses **Inter** for its neutral, highly legible characteristic at small sizes on mobile screens.
- **Labels & Metadata:** Uses **JetBrains Mono** for technical data like timestamps, frame counts, and file sizes, reinforcing the "pro tool" aesthetic.

All tracking is tightened slightly for headlines to maintain a compact, premium appearance, while labels feature increased tracking for readability at very small scales.

## Layout & Spacing

The layout utilizes a fluid grid tailored for high-frequency interaction. To accommodate video editing workflows, the vertical axis is prioritized for the timeline and preview, while the horizontal axis utilizes edge-to-edge containers.

- **Touch Targets:** All interactive elements must maintain a minimum 44x44px area to ensure accuracy during fast-paced editing.
- **Timeline Rhythm:** The editing timeline uses a 4px base unit for snapping and zooming increments.
- **Margins:** A standard 16px lateral margin is enforced across all screens to prevent UI elements from clashing with hardware edges or rounded screen corners.

## Elevation & Depth

This design system uses **Tonal Layering** and **Glassmorphism** to communicate hierarchy.

1.  **Level 0 (Base):** The main canvas (`#0F0F10`).
2.  **Level 1 (Panels):** Toolbars and secondary panels use a slightly lighter gray with a 10% opacity white stroke to define boundaries.
3.  **Level 2 (Modals/Overlays):** These utilize a "Material" blur (Backdrop Filter) with 80% opacity, creating a frosted glass effect that keeps the video content visible underneath while focusing the user's attention.
4.  **Shadows:** Shadows are rarely used for depth; instead, "Inner Glows" (subtle top-edge highlights) are applied to buttons to give them a tactile, slightly extruded feel without appearing skeuomorphic.

## Shapes

The shape language is consistently "Rounded" to mirror the hardware curvature of modern iPhones.

- **Standard Elements:** Buttons, input fields, and cards use a 0.5rem (8px) radius.
- **Large Containers:** Bottom sheets and full-screen cards use a 1.5rem (24px) top-radius.
- **Timeline Clips:** Video clips in the track view use a 4px radius to maximize screen real estate while remaining soft to the touch.

## Components

### Buttons
- **Primary:** Solid Cyan (`#00C2FF`) with Black text. High-gloss finish.
- **Secondary:** Surface-colored with a subtle 1px border.
- **Icon Buttons:** Circular or Rounded-Square with a background blur when overlaid on video.

### Timeline Clips
- Clips should have a 1px inner border to separate them from adjacent clips. 
- Active clips are outlined with a 2px Cyan border and a subtle outer glow.

### Input Fields
- Underlined or softly enclosed styles.
- Focused states use a Cyan glow and a persistent label using the `label-sm` style.

### Chips & Tags
- Used for effect categories or media tags. 
- Small, compact, and utilizing the `label-lg` font style for technical clarity.

### Sliders & Scrubbers
- Custom scrubbers with a vertical "playhead" line. 
- Handles must be at least 24px wide for easy thumb-grabbing while remaining visually thin (2px-4px).