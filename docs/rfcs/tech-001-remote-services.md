## Remote services encapsulation

Given the application requirements[^1], we must use cloud-based services for the inner workings of the application. The use of cloud services, such as Firebase is recommended, but this requires Google Play Services[^2] installed on the user's device[^3].

In order to allow users without Google Play services installed to use our application, every remote resource MUST be encapsulated using Java interfaces.
These interfaces MUST be located in the `ch.epfl.sweng.erpa.services` package. Implementations of such interfaces MUST be located under `ch.epfl.sweng.erpa.services.<provider>` package (e.g. `ch.epfl.sweng.erpa.services.firebase`).
These services MUST have an implementation using Cloud-based services (such as Firebase). Other implementations not using any Cloud-based integration are also possible.
The user USER be asked when the application is first launched which provider to use. The user MUST be able to change this parameter from the application settings.
A single provider MUST be in use at all times.


## Remote data storage backend

All the remote data required by the application MUST be stored in the Cloud Firestore Database[^4].
Users MUST be able to use the firebase storage regardless of their authentication method. Plain username (or email) and KDF[^5]-derived key MUST be supported. The KDF MUST be public so that any Authentication provider can sign in the user regardless of how the user was created.
Authentication providers MAY store the derived key, but MUST NOT store the original secret.



[^1]: http://dslab.epfl.ch/teaching/sweng/proj
[^2]: https://en.wikipedia.org/wiki/Google_Play_Services
[^3]: https://firebase.google.com/docs/android/setup
[^4]: https://firebase.google.com/docs/firestore/
[^5]: https://en.wikipedia.org/wiki/Key_derivation_function
