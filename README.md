# Hipo Exceptions Android 

![Bintray](https://img.shields.io/bintray/v/hipo/HipoExceptions/com.hipo.hipoexceptionsandroid)

Hipo exceptions library is a Kotlin library to parse API error json.

This library can parse jsons below;
* Empty body json
```
{}
```

* Simple double error message: Shows first error, which is "amount" for the example below

```
"detail": {
  "amount": [
    "A valid integer is required. (not fallback)"
  ],
  "description": [
    "This field may not be blank. (not fallback)"
  ]
}
```
* Nested double error message: Shows first error, which is "amount" for the example below

```
"detail": {
    "amount": {
      "type": [
        "Amount Type is False. (not fallback)"
      ]
    },
    "email": {
      "type": [
        "Not compatible type of email. (not fallback)"
      ]
    }
  }
```

* Simple error message: Shows error message, not fallback.

```
"detail": {
  "email": [
    "Email is already registered. (not fallback)"
  ]
}
```

* Nested error message: Shows error message, not fallback.

```
"detail": {
  "amount": {
    "type": [
      "Amount Type is False. (not fallback)"
    ]
  }
}
```

* Non-field error message: Shows first error

```
"detail": {
  "non_field_errors": [
    "Profile credentials are not correct. (not fallback)",
    "Email is not registered before."
  ]
}
```    

* Null json: Shows default error message

```
{
  "type": null,
  "detail": null,
  "fallback_message": null
}
```

* Only fallback message: Shows fallback message

```
{
  "fallback_message": "Email is already registered."
}
```

* Too much nested error message: Shows first error message, which is "amount" for the example below

```
"detail": {
  "amount": {
    "type": {
      "type2": {
        "type3": {
          "type4": {
            "amount": [
              "Error. (not fallback)"
            ],
            "type5": {
              "type6": {
                "isPaid": [
                  "isPaid cannot be send as null. (not fallback)"
                ]
              }
            }
          }
        }
      }
    }
  },
  "email": {
    "type": [
      "Not compatible type of email. (not fallback)"
    ]
  }
}
```

## Installation
Add below implementation line into app module build.gradle

![Bintray](https://img.shields.io/bintray/v/hipo/HipoExceptions/com.hipo.hipoexceptionsandroid)

```
implementation 'com.github.Hipo:hipo-exceptions-android:[LatestVersion]'
```

## Usage

Takes three parameters;
* Gson
* defaultErrorMessage =  To show if json doesn't contain any error message.
* responseCodesToLog = To log exception to crashlytics if crashlytics is initialized.

```
val errorHandler = RetrofitErrorHandler(
            gson,
            defaultErrorMessage = application.getString(R.string.default_error_message),
            responseCodesToLog = intArrayOf(HTTP_CODE_402)
)

// import retrofit2.Response
val parsedError = errorHandler.parse(response)

//data class ParsedError(
//    val keyErrorMessageMap: Map<String, List<String>>,
//    val message: String,
//    val responseCode: Int
//)
```
