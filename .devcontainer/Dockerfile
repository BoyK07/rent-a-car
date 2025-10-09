FROM mcr.microsoft.com/devcontainers/java:1-21-bullseye

# Arguments for build customization
ARG INSTALL_MAVEN="false"
ARG MAVEN_VERSION=""

ARG INSTALL_GRADLE="true"
ARG GRADLE_VERSION=""

# Rename vscode user to container
RUN usermod -l container vscode \
    && groupmod -n container vscode \
    && usermod -d /home/container -m container

# Set environment for SDKMAN installs under the new user
RUN if [ "${INSTALL_MAVEN}" = "true" ]; then su container -c "umask 0002 && . /usr/local/sdkman/bin/sdkman-init.sh && sdk install maven \"${MAVEN_VERSION}\""; fi \
    && if [ "${INSTALL_GRADLE}" = "true" ]; then su container -c "umask 0002 && . /usr/local/sdkman/bin/sdkman-init.sh && sdk install gradle \"${GRADLE_VERSION}\""; fi

# [Optional] Uncomment this section to install additional OS packages.
# RUN apt-get update && export DEBIAN_FRONTEND=noninteractive \
#     && apt-get -y install --no-install-recommends <your-package-list-here>

# [Optional] Uncomment this line to install global node packages.
# RUN su container -c "source /usr/local/share/nvm/nvm.sh && npm install -g <your-package-here>" 2>&1
