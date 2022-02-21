# Penguin

This is a simple study app that simulates money transactions between different countries checking their local exchange rates with the Open Exchange Rate API and converting valus to Binary.

### API
https://openexchangerates.org/api/
For security purposes API ID/KEY is being defined in the local.settings file and loaded into the build.gradle file, to include your ID/KEY add it in the following format:

```
// local.settings
api.id="YOUR_ID"
```

### Technical Specifications
- Architecute: MVVM with Repository
- Dependency Injection: Koin
- Network: Retrofit with Moshi and Coroutines
- Mock: MockK
