import SwiftUI
import Shared

class SwiftSignInHandler: NSObject, GoogleAuthRepositorySignInHandler {
    let authRepository: GoogleAuthRepository

    init(authRepository: GoogleAuthRepository) {
        self.authRepository = authRepository
        super.init()
    }

    func onSignInRequested() {
        // Note: Real Google Sign-in on iOS uses the GIDSignIn SDK.
        // We present a mock simulation/trigger or call GIDSignIn if the package is linked.
        // Since we are running in cross-platform environment, we can fetch token and login.
        // To build cleanly if GoogleSignIn SDK is not linked yet, we can do a mock login
        // which returns a simulated Google token to test the Drive synchronization immediately,
        // or integrate GIDSignIn. Let's provide a simulation that calls the shared repository.
        // If you link GoogleSignIn in Xcode, you can replace this with:
        // GIDSignIn.sharedInstance.signIn(withPresenting: rootVC, additionalScopes: ["https://www.googleapis.com/auth/drive.file"]) { ... }
        
        print("Google Sign-In requested on iOS")
        
        // Simulating successful Google sign in with a mock token to allow testing Drive syncing
        let mockAccessToken = "ya29.mock-token-for-testing-google-drive-syncing-12345"
        self.authRepository.fetchProfileAndSetSession(token: mockAccessToken) { _ in }
    }

    func onSignOutRequested() {
        print("Google Sign-Out requested on iOS")
    }

    func onLaunchIntent(intentSender: Any) {
        // Not used on iOS
    }
}

@main
struct iOSApp: App {
    private let signInHandler: SwiftSignInHandler

    init() {
        KoinKt.doInitKoin(appDeclaration: nil)

        let authRepo = KoinHelper().getGoogleAuthRepository()
        self.signInHandler = SwiftSignInHandler(authRepository: authRepo)
        authRepo.setSignInHandler(handler: self.signInHandler)
    }

    var body: some Scene {
        WindowGroup {
            ContentView()
        }
    }
}
