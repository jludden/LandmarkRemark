# Tigerspike - Landmark Remark Android App

App built for Tigerspike application process. Users can pin landmarks and add messages to them in a map application. The landmark remarks are synced across firebase to other users in realtime. [Test it out in the web browser!](https://appetize.io/app/ TODO)

Please see the attached .pdf for the full project requirements.

## Design
The requirements for this project strictly disallow any third-party libraries and require a backend-as-a-service provider. I chose to use Google Firebase due to its easy free tier. Similarly, I had no choice but to implement the maps using a Google Maps fragment in Android. From there, it was eas 


- Google Maps
- Firebase

## Testing
Launch the application with one of the following methods:
- [Test it out in the web browser!](https://appetize.io/app/ TODO)
- Open the project in Android Studio
	- debug using an emulator or usb device
	- or build apk and load it onto a device
- Load the debug apk included in the zipped project files (not included in git repo)

## Limitations
- Not very useable in China or other countries where Google services are blocked
- Somewhat reliant on location permissions
	- if they say no, the app can still be used to look at and search landmarks. However, they won't be able to add their own.
	- if someone deny's the location request, then closes and reopens the app, they will be reprompted and will have the ability to allow the request

## Time estimation
As mentioned in the project requirements, here is a conservative estimate of the time taken for the various parts of the application development:
- Initial firebase sync - 1.5hr
- Current location + permissions - 2hr
- Re-doing current location + permissions - 1.5hr
- Adding landmarks - 1 hr
- Searching landmarks - 2hr
- Final testing, code cleanup, documentation - 2 hr