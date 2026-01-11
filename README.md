# Afterimages

### Usage

This mod is entirely data-driven, meaning you can configure which entities have afterimages and customize their appearance using resource packs.

#### Adding Afterimages to Entities

To add an afterimage to an entity, create a JSON file in the following directory structure within your resource pack:

`assets/<namespace>/afterimages/entities/<entity_name>.json`

**Example:**
To give an Arrow an afterimage, create a file at `assets/minecraft/afterimages/entities/arrow.json`:

```json
{
  "speed_threshold": 0.5,
  "duration": 10
}

```

#### Configuration Fields

The JSON configuration accepts the following fields:

* **`speed_threshold`**: The minimum speed (in blocks per tick) the entity must be moving to generate afterimages. Defaults to `0.5`.
* **`duration`**: How long the afterimage trail lasts in ticks. Defaults to `10`.
* **`entity`**: The ID of the entity (e.g., `"minecraft:ender_pearl"`) or an entity type tag (e.g., `"#minecraft:arrows"`) to apply the effect to. This is optional; if omitted, the mod will infer the entity type from the JSON filename and namespace.
* **`color`**: A hex color value to tint the afterimage. Defaults to `0xFFFFFF`.
* **`overlay_only`** (Boolean): Defaults to `false`.

### License

This project is licensed under the **MIT License**.

---

### Contributing

Contributions are welcome! If you find a bug or have a feature request, please open an issue or submit a pull request.