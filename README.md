Sunshine Application Project

From Udacity Course: Developing Android Apps by Google

Changes and updates/improvements:

    - Changed codebase from Java to Kotlin
    - DetailActivity:
        - Changed to data object binding instead of setting each child views data in the Activity
    - ForecastAdapter:
        - Changed from ViewHolder holding references to view components and setting each components
            data to data binding for each item
    - gsonResponse package:
        - Created new package to hold Gson converter data classes for the Retrofit response
    - NetworkUtils
        - Changed HttpUrlConnection to Retrofit
        - Added methods to create queryMap for Retrofit
    - SunshineSyncTask
        - Changed NetworkUtils call from getResponseFromHttpUrl() to newly created 
            getResponseFromRetrofit()
    - Updated GET request to call OpenWeatherMap API