import SwiftUI
import GoogleMaps

@main
struct iOSApp: App {

    init() {
        GMSServices.provideAPIKey("AIzaSyCVxtZiAREFazDjXPW2pMNzc2B9EN0FFBc")
    }

	var body: some Scene {
		WindowGroup {
			ContentView()
		}
	}
}
