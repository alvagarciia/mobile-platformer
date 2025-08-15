<a id="readme-top"></a>
<!--
*** ReadMe template from Othneil Drew @othneildrew on GitHub
-->
<!-- [![project_license][license-shield]][license-url]
[![LinkedIn][linkedin-shield]][linkedin-url] -->


<!-- PROJECT LOGO -->
<br />
<div align="center">
  <a href="https://github.com/alvagarciia/mobile-platformer">
    <img src="media/logo.png" alt="Logo" width="80" height="80">
  </a>

<h3 align="center">Mobile Platformer</h3>

  <p align="center">
    Platformer game made for mobile devices
    <br />
    <br />
    <a href="https://github.com/alvagarciia/mobile-platformer/issues/new?labels=bug&template=bug-report---.md">Report Bug</a>
    &middot;
    <a href="https://github.com/alvagarciia/mobile-platformer/issues/new?labels=enhancement&template=feature-request---.md">Request Feature</a>
  </p>
</div>



<!-- TABLE OF CONTENTS -->
<details>
  <summary>Table of Contents</summary>
  <ol>
    <li>
      <a href="#about-the-project">About The Project</a>
      <ul>
        <li><a href="#features">Features</a></li>
        <li><a href="#built-with">Built With</a></li>
      </ul>
    </li>
    <li>
      <a href="#getting-started">Getting Started</a>
      <ul>
        <li><a href="#prerequisites">Prerequisites</a></li>
        <li><a href="#option1">Option 1</a></li>
        <li><a href="#option2">Option 2</a></li>
      </ul>
    </li>
    <li>
      <a href="#usage">Usage</a>
      <ul>
        <li><a href="#demo">Demo</a></li>
      </ul>
    </li>
    <li><a href="#contributing">Contributing</a></li>
    <li><a href="#license">License</a></li>
    <li><a href="#3p-assets">About 3rd Party Assets</a></li>
    <li><a href="#contact">Contact</a></li>
  </ol>
</details>



<!-- ABOUT THE PROJECT -->
## About The Project

![MP Showcase](./media/mp-show.jpg)

This web application predicts the market price of used cars based on various features such as mileage, brand, model, fuel type, and more. Built using Python and Streamlit, the app leverages machine learning models to provide pricing estimates, assisting both buyers and sellers in making informed decisions.

My personal objective with this project was to put in practice everything I learnt for working with Machine Learning and Deep Learning models by using it to create a piece of software. In addition, I aimed to experiment creating this app using Streamlit, which I hadn't used prior to this. 

Both models were trained from scratch using a Kaggle [dataset](https://www.kaggle.com/datasets/taeefnajib/used-car-price-prediction-dataset/data) uploaded by Taeef Najib, made up of a collection of automotive information extracted from the popular automotive marketplace website, https://www.cars.com. This dataset comprises 4,009 data points, each representing a unique vehicle listing, and includes nine distinct features providing valuable insights into the world of automobiles.

<p align="right">(<a href="#readme-top">back to top</a>)</p>


### Features

- User input form for entering car specifications
- Pre-trained ML and DL models for price prediction
- Built-in preprocessing (encoding, imputation, etc.)
- Works from serialized models using `.joblib` and `.keras` files

<p align="right">(<a href="#readme-top">back to top</a>)</p>


### Built With

* [![Gradle][gradle]](https://github.com/gradle/gradle)
* [![Android Studio][androids]](https://developer.android.com/studio)

<p align="right">(<a href="#readme-top">back to top</a>)</p>



<!-- GETTING STARTED -->
## Getting Started

You can explore this project in two main ways:
1. **Run the pre-built `.apk`** on your Android device or emulator (fastest).
2. **Open the source code in Android Studio** to explore, modify, and build it yourself.

### Prerequisites

**For .apk file:**
- An Android device or emulator (Android 5.0+ recommended).
- Optionally, enable "Install from Unknown Sources" in device settings.

**For full source build:**
- [Android Studio](https://developer.android.com/studio) installed.
- JDK (Java Development Kit) — included with most Android Studio installations.
- Internet connection for Gradle to download dependencies on first build.

### Option 1 — Run the `.apk` (Quick Demo)
1. Download the `mobile-platformer.apk` file from the `/release` folder.
2. Transfer it to your Android device or load it into an Android emulator.
3. Open it to install the app.  
   *(You may need to enable "Install from Unknown Sources" in your device settings.)*

**Note:**  
The `.apk` is provided for demonstration purposes and may not be fully optimized for all screen sizes. For the most consistent experience, try running it in an Android Studio emulator.

---

### Option 2 — Open in Android Studio (Full Source)
1. Clone the repository:
   ```sh
   git clone https://github.com/alvagarciia/mobile-platformer.git
   cd mobile-platformer
   ```
2. Open the project in Android Studio.
3. Allow Gradle to sync and download dependencies.
4. Select a device or emulator from the Android Studio device manager. The `Pixel 4a` device was used for production.
5. Click **Run** to build and launch the app.


<p align="right">(<a href="#readme-top">back to top</a>)</p>



<!-- USAGE EXAMPLES -->
## Usage

Once ran on Streamlit, users have the option of entering their own car data or using a pre-defined test case. For each option, they also have the option of getting a prediction using the Machine Learning model or the Deep Learning model. 

After selecting either of the options, the respective model should display the prediction on the website.

The repo also contains the scripts used to train each model inside the `scripts` folder, so that anyone can take a look at how these models were trained. Moreover, the `models` folder contains the trained files for both the ML and DL models.

<p align="right">(<a href="#readme-top">back to top</a>)</p>



### Demo

![Platformer Demo](./media/platformer-demo1.gif)
![Platformer Demo](./media/platformer-demo2.gif)

<p align="right">(<a href="#readme-top">back to top</a>)</p>



<!-- CONTRIBUTING -->
## Contributing

Contributions are what make the open source community such an amazing place to learn, inspire, and create. Any contributions you make are **greatly appreciated**.

If you have a suggestion that would make this better, please fork the repo and create a pull request. You can also simply open an issue with the tag "enhancement".
Don't forget to give the project a star! Thanks again!

1. Fork the Project
2. Create your Feature Branch (`git checkout -b feature/AmazingFeature`)
3. Commit your Changes (`git commit -m 'Add some AmazingFeature'`)
4. Push to the Branch (`git push origin feature/AmazingFeature`)
5. Open a Pull Request

<p align="right">(<a href="#readme-top">back to top</a>)</p>



<!-- LICENSE -->
## License

Distributed under the MIT License. See `LICENSE` for more information.

<p align="right">(<a href="#readme-top">back to top</a>)</p>



<!-- 3RD PARTY ASSETS -->
## About 3rd Party Assets

This project contains visual assets that are the property of their respective owners.
Nintendo and related characters are trademarks of Nintendo. 
These assets are included here for educational purposes only and are not licensed for commercial use.

If you reuse this code, replace these assets with your own or with freely available alternatives.

<p align="right">(<a href="#readme-top">back to top</a>)</p>



<!-- CONTACT -->
## Contact

Alvaro Garcia - [LinkedIn @alvaro-garciam](https://www.linkedin.com/in/alvaro-garciam) - alvaroedgamu@gmail.com

Project Link: [https://github.com/alvagarciia/mobile-platformer](https://github.com/alvagarciia/mobile-platformer)

<p align="right">(<a href="#readme-top">back to top</a>)</p>



<!-- MARKDOWN LINKS & IMAGES -->
<!-- 
https://www.markdownguide.org/basic-syntax/#reference-style-links 
https://simpleicons.org/
-->
[license-s
hield]: https://img.shields.io/github/license/alvagarciia/mobile-platformer.svg?style=for-the-badge
[license-url]: https://github.com/alvagarciia/mobile-platformer/blob/main/LICENSE
[linkedin-shield]: https://img.shields.io/badge/-LinkedIn-black.svg?style=for-the-badge&logo=linkedin&colorB=555
[linkedin-url]: https://linkedin.com/in/alvaro-garciam


[gradle]: https://img.shields.io/badge/Gradle-02303A?style=for-the-badge&logo=gradle&logoColor=white
[androids]: https://img.shields.io/badge/Android_Studio-3DDC84?style=for-the-badge&logo=androidstudio&logoColor=white
[json]: https://img.shields.io/badge/JSON-000000?style=for-the-badge&logo=json&logoColor=white