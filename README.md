# App Development

## Setup Instructions

### Step 1: Clone the Repository

- Clone this repository to your local machine using Git:
  ```bash
  git clone git@github.com:LucasLubbers/App-development.git .
  ```

### Step 2: Configure Properties File

- Copy the contents of `gradle.properties.example` into a new file named `gradle.properties` in the project root:

### Step 3: Add Supabase API Key

- Obtain your API key from your Supabase project dashboard
- Open the newly created `gradle.properties` file and replace the placeholder value for `supabase_api_key` with your actual key:
  ```properties
  supabase_api_key=your_actual_supabase_key
  ```

### Step 4: Open Project in Android Studio

- Launch **Android Studio** (Meerkat version or newer).
- Open the project folder and wait for **Gradle to finish building** the project.

### Step 5: Run the App

You have two options to run the app:

#### Option A: Run on a Physical Android Device
- Connect your Android phone via USB.
- Enable **USB Debugging** in developer settings on your phone.
- Click **Run** in Android Studio and select your connected device.

#### Option B: Run on Android Emulator
- Start an emulator via Android Studio.
- Click **Run** and select the emulator.

> ⚠️ **Note:** Some functionalities may not work correctly when using the emulator due to limitations (e.g., sensors, camera, location tracking, push notifications, ).
