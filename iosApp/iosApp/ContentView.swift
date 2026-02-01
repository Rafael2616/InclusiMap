import SwiftUI
import UIKit
import Navigation

struct ComposeEventsView: UIViewControllerRepresentable {
    func makeUIViewController(context: Context) -> UIViewController {
        NavHostKt.inclusimapNavHost()
    }

    func updateUIViewController(_ uiViewController: UIViewController, context: Context) {
    }
}

struct ContentView: View {
    var body: some View {
        ComposeEventsView()
            .edgesIgnoringSafeArea(.all)
    }
}
