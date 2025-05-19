# App development


# PocketBase Setup Guide

Follow these steps to get PocketBase up and running with Docker and configure your superuser.

### Prerequisites

* Docker installed
* Docker Compose installed

---

## Steps

1. **Start Docker**

   Ensure your Docker daemon is running.

2. **Remove existing `pb_data` directory**

   Make sure the `pb_data` directory is deleted or not present to avoid conflicts:

   ```bash
   rm -rf pb_data
   ```

3. **Build and start the containers**

   Run the following command to build and start your Docker containers:

   ```bash
   docker-compose up --build
   ```

4. **Open the Superuser Registry URL**

   When the container starts, look for a message like this:

   ```
   (!) Launch the URL below in the browser if it hasn't been opened already to create your first superuser account:
   pocketbase-1  | http://0.0.0.0:8090/_/#/pbinstal/
   ```

   Open that URL in your browser.

   * If the page doesn't load correctly, replace `0.0.0.0` with `localhost`.

5. **Create your superuser**

   Use the UI to create your first superuser account.
   Then, save the email and password you used into your `.env` file.

6. **Generate and save the API key**

   * In PocketBase, go to `System > _superusers`.
   * Click on your created superuser.
   * Click the three dots in the top right corner.
   * Click **Impersonate**.
   * Click **Generate** to create an API key.
   * Copy the API key and save it to your `.env` file.

7. **Restart Docker containers**

   Stop and remove containers and volumes, then restart:

   ```bash
   # Stop container
   Ctrl+C

   # Remove containers and volumes
   docker-compose down --volumes

   # Rebuild and start containers
   docker-compose up --build
   ```

8. **PocketBase is now running**

   Your PocketBase instance will start with the standard collections and records.
