<!-- PROJECT SHIELDS -->
<!--
*** I'm using markdown "reference style" links for readability.
*** Reference links are enclosed in brackets [ ] instead of parentheses ( ).
*** See the bottom of this document for the declaration of the reference variables
*** for contributors-url, forks-url, etc. This is an optional, concise syntax you may use.
*** https://www.markdownguide.org/basic-syntax/#reference-style-links
-->


<!-- PROJECT LOGO -->
[简体中文](/README_ZH.md) | English
<br />
<p align="center">
  <a href="https://github.com/Meodinger/LabelPlusFX">
    <img src="images/logo.png" alt="Logo" width="80" height="80" />
  </a>
  <h3 align="center">Label Plus FX</h3>
  <p align="center">
    A cross-platform LabelPlus
    <br />
    <br />
    <a href="https://www.kdocs.cn/l/cpRyDN2Perkb">View Manual</a>
    ·
    <a href="https://github.com/Meodinger/LabelPlusFX/issues">Report Bug</a>
    ·
    <a href="https://github.com/Meodinger/LabelPlusFX/issues">Request Feature</a>
  </p>
</p>


<!-- TABLE OF CONTENTS -->
<details open="open">
  <summary><h2 style="display: inline-block">Table of Contents</h2></summary>
  <ol>
    <li>
      <a href="#about-the-project">About The Project</a>
    </li>
    <li>
      <a href="#getting-started">Getting Started</a>
      <ul>
        <li><a href="#prerequisites">Prerequisites</a></li>
        <li><a href="#installation">Installation</a></li>
      </ul>
    </li>
    <li><a href="#usage">Usage</a></li>
    <li><a href="#license">License</a></li>
    <li><a href="#contact">Contact</a></li>
  </ol>
</details>


<!-- ABOUT THE PROJECT -->
## About The Project

[![Product Screen Shot][product-screenshot]]()

This project is inspired by [LabelPlus](https://noodlefighter.com/label_plus/).
Because there is only a C# version, I wrote a javafx version for mac/linux user.


<!-- GETTING STARTED -->
## Getting Started

To get a local copy up and running follow these simple steps.


### Prerequisites

 * [Liberica JDK 17 (Full)](https://bell-sw.com/pages/downloads/#/java-17-lts%20/%20current) : For main application;

 * [Optional] [Visual Studio 2019](https://visualstudio.microsoft.com/zh-hans/downloads/) : For Windows IME JNI Interface;


### Installation

1. Clone the repo
   ```sh
   git clone https://github.com/Meodinger/LabelPlusFX.git
   ```
2. Run maven build `package`

3. Build with script, both `link.bat` and `build.bat` is OK

4. For Windows User, build solution `IMEWrapper` and copy the `IMEInterface.dll` and `IMEWrapper.dll` to the same folder with `LabelPlusFX.exe` if used `jpackage` or `runtime\java.exe` if used `jlink`.

> If you don't want to use the Windows IME JNI Interface, try `run.bat --disable-jni` or `LabelPlusFX.exe --disable-jni`

> If you want to run LPFX in IDE, execute maven goal `exec:java@run` to start the application

<!-- USAGE EXAMPLES -->
## Usage

Label Plus FX's function design based on [LabelPlus](https://noodlefighter.com/label_plus/)

_For more examples, please refer to the [User Manual](https://www.kdocs.cn/l/seRSJCKVOn0Y) and [Wiki](https://github.com/Meodinger/LabelPlusFX/wiki)_


<!-- CONTRIBUTING -->
## Contributing

Contributions are what make the open source community such an amazing place to learn, inspire, and create. Any contributions you make are **greatly appreciated**.

1. Fork the Project
2. Create your Feature Branch (`git checkout -b feature/AmazingFeature`)
3. Commit your Changes (`git commit -m 'Add some AmazingFeature'`)
4. Push to the Branch (`git push origin feature/AmazingFeature`)
5. Open a Pull Request



<!-- LICENSE -->
## License

Distributed under the AGPLv3 License. See `LICENSE` for more information.


<!-- CONTACT -->
## Contact

Meodinger Wang - [@Meodinger_Wang](https://twitter.com/Meodinger_Wang) - meodinger@qq.com

Project Link: [https://github.com/Meodinger/LabelPlusFX](https://github.com/Meodinger/LabelPlusFX)

<!-- SPONSOR -->

## Sponsor

<a href="https://afdian.net/@Meodinger">
  <img src="https://s2.loli.net/2022/04/01/p4kequKy9g7EMZb.jpg" alt="Aifadian" width="375" />
</a>

[product-screenshot]: https://s2.loli.net/2022/02/04/2H7bguJ9rcyBjUO.png
