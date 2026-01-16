## Development Environment (JetBrains Gateway + Dev Containers)

This project uses **JetBrains Gateway** to provide a consistent Docker-based development environment defined by a **Dev Container**.

Gateway lets you open this container directly — without using IntelliJ’s built-in Docker integration.

---

### Requirements

- [JetBrains Gateway](https://www.jetbrains.com/remote-development/gateway/)
- Docker Desktop (or compatible local Docker engine)
- JetBrains IDE (IntelliJ IDEA, PyCharm, WebStorm, etc.)
- **Dev Containers plugin** (`com.jetbrains.devcontainers`)
- Access to this repository’s Git URL (for the VSC Project flow)

---

### Opening the Project in a Dev Container

1. **Start JetBrains Gateway**  
   Open the standalone **JetBrains Gateway** app.

2. **Select “Dev Containers”**  
   In the left sidebar, click **Dev Containers**.  
   Gateway detects your Docker engine and lists existing containers.

3. **Create a New Container**  
   If none exists yet, click **“New Dev Container”** in the **top-right corner**.

4. **Choose how to start the project:**

---

### Option 1 — From VSC Project (recommended for first-time setup)

This downloads the repository directly into the Dev Container (no local mounts).

- **Docker Instance**: Select your Docker engine (default = local Docker).  
- **IDE**: Open the dropdown and select **IntelliJ IDEA** (or another JetBrains IDE).  
- **Git Repository**: Enter the SSH URL of this repo  
  (for example: `git@github.com:your-org/your-repo.git`)  
- **Dev Container Detection**: Leave set to *Automatic* if possible.  
  - If detection fails, choose **Specify Path** → `.devcontainer/devcontainer.json`  
    (this is the default expected path).

Gateway will:

1. Clone the repo inside the container.
2. Build the container image.
3. Launch the chosen JetBrains IDE backend inside the container.
4. Attach your local JetBrains Client automatically.

This approach is **faster and more isolated** because it avoids mounting your local filesystem.

---

### Option 2 — From Local Project

Use this if you already have the repository cloned locally.

- **Project Source**: Choose **Local Folder** and point to your local project root.  
- **Dev Container Path**: Always select **Specify Path** and enter `.devcontainer/devcontainer.json`.  
- **Docker Instance**: Choose the same as above (local Docker).  
- **IDE**: Select your JetBrains IDE (e.g. IntelliJ IDEA).

Gateway will mount your local project into the container and attach the IDE.  
This is slightly slower than cloning inside the container but works without Git access from Docker.
