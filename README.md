# JobMarket

Android app for browsing the remote job market. Course project for Mobile Application Development (USTH), topic 26 "Job market".

Data comes from the public [Remotive API](https://remotive.com/api/remote-jobs) (no key needed). If the phone is offline the app shows the last saved copy, and if there is none, a small bundled sample so the demo never shows an empty screen.

## Features

| Tab | What it does |
|---|---|
| Jobs | Latest postings. Filter by category (server side), search by title, company or skill (local). Tap a card for details, tap the bookmark or swipe a card to save it. |
| Insights | Numbers computed from the loaded postings: totals, share with salary, jobs per category, contract type and top hiring companies. |
| Saved | Bookmarked jobs, available offline. Swipe to remove. |

Also: job detail screen (apply in browser, share), English and Vietnamese UI, switchable in Settings.

## Run it

1. Android Studio (Koala or newer), JDK 17.
2. Open this folder, let Gradle sync.
3. Run on an emulator or a phone (Android 5.0+). The first start needs internet.

Not tested on a device when this skeleton was generated, so expect to fix small build issues on first sync.

## Where the course topics show up

| Course topic | Where |
|---|---|
| Activity, Intent, lifecycle | `MainActivity`, `JobDetailActivity`, `SettingsActivity` |
| Fragment, ViewPager, TabLayout | `MainPagerAdapter`, `*Fragment` |
| Application class (global data) | `JobMarketApp` |
| Layouts, `layout_weight` | `res/layout`, bars in `item_bar.xml` |
| Values, i18n | `res/values`, `res/values-vi` |
| Drawables, assets | `res/drawable`, `assets/sample_jobs.json` |
| Menu, Toolbar | `menu_main.xml`, `MainActivity` |
| Thread + Handler | `JobRepository.load()` |
| AsyncTask + HttpURLConnection | `util/ImageLoader` (logos) |
| Volley + JSON | `JobRepository.fetch()`, `JobParser` |
| Storage (private files, SharedPreferences) | offline cache in `JobRepository`, `BookmarkStore` |
| Touch gestures | swipe-to-save in `JobAdapter.attachSwipe()` |
| Permissions | `INTERNET` in the manifest |

## Architecture

```
 Fragments / Activities (controllers)
        |  listen                |  tap, swipe
        v                        v
  JobRepository  <---------  BookmarkStore        (data layer, owned by JobMarketApp)
   |   |    |
 Volley cache  assets/sample                      Job = model
```

Every screen observes `JobRepository` and `BookmarkStore` in `onStart()` and stops in `onStop()`, so the three tabs always show the same data without passing lists around.

## Known limits

- `AsyncTask` and `FragmentPagerAdapter` are deprecated. They are used on purpose because the course teaches them; the alternatives (executors, ViewPager2) are listed under "possible future development".
- Remotive asks API users to link back to the posting and credit them. The Apply button opens their URL and the banner names the source; check their terms again before publishing the app.
- Salary text is free-form, so Insights only counts whether a salary is present.
