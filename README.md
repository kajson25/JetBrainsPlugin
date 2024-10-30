# Layered Architecture Generator

Layered Architecture Generator is an IntelliJ IDEA plugin designed to streamline the creation of supporting classes for entities in Java and Kotlin projects. By automating the generation of `Repository`, `Service`, `Controller`, `Dto`, and `Mapper` classes, this plugin helps you maintain a clean, layered architecture.

## Features

- Automatically generate supporting classes (`Repository`, `Service`, `Controller`, `Dtos`, `Mapper`) upon entity creation.
- Compatible with Java (preferably Java 17) and Kotlin.
- Automatically organizes generated classes into folders such as `repository`, `service`, `controller`, `mapper`, and `dtos`.
- Configurable packages for entity generation.

## Installation

1. Clone the repository or download the plugin file.
2. Open IntelliJ IDEA, then go to **File > Settings > Plugins**.
3. Click **Install Plugin from Disk...**, select the plugin file, and follow the installation prompts. (adding my plugin to intelliJ marketplace is in process)
4. Once installed, restart IntelliJ IDEA.

## Usage

1. **Create an Entity**: When you create a new entity in your project, a prompt will appear, asking if you’d like to generate the corresponding supporting classes.
2. **Select Supporting Classes**: You can choose to generate `Repository`, `Service`, `Controller`, `Dto`, and `Mapper` classes.
3. **Generated Classes**: The plugin will automatically create the selected classes in their respective folders: `repository`, `service`, `controller`, `mapper`, and `dtos`.

## Configuration

To specify which packages should trigger the generation feature, you can configure the plugin in two ways:

1. **Tools > Set Entity Packages**  
   Enter package names that should trigger the generation (comma-separated if multiple).

2. **File > Settings > My Plugin Settings**  
   Configure entity package names directly in the settings UI.

> **Note**: If the required folders (`repository`, `service`, `controller`, `mapper`, and `dtos`) don’t exist, the plugin will create them automatically. Be sure to specify packages from the root Java/Kotlin package, for example, `com.example.mypackage`.

## Project Configuration

Here's a quick overview of the plugin’s configuration files:

- **Plugin ID**: `myplugin.layeredarchitecturegenerator`
- **Version**: 1.1.0
- **Vendor**: Katarina Vucicevic, [email](mailto:katarina.vucicevic25@gmail.com)

### Development Environment

- **IntelliJ Platform Version**: 2023.2
- **Kotlin Version**: 1.8.10
- **Java Compatibility**: Minimum build version 231, up to build 233.\*

## License

This project is licensed under the MIT License. See the [LICENSE](LICENSE) file for details.

## Support

If you encounter any issues or have questions, feel free to reach out via email at [katarina.vucicevic25@gmail.com](mailto:katarina.vucicevic25@gmail.com).
