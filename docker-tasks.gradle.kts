import org.gradle.api.tasks.Exec

val serviceName = "slack-emoji-service"
val containerName = "asyncsite-$serviceName"
val imageName = "asyncsite/$serviceName"
val networkName = "asyncsite-network"

tasks.register<Exec>("dockerBuild") {
    group = "docker"
    description = "Build Docker image for $serviceName"
    commandLine("docker", "build", "-t", imageName, ".")
}

tasks.register<Exec>("dockerRun") {
    group = "docker"
    description = "Run $serviceName container"
    dependsOn("dockerBuild")

    // Allow overriding Slack credentials and redirect URI from host env
    val slackRedirectUri = System.getenv("SLACK_REDIRECT_URI")
        ?: "http://localhost:8080/api/public/slack-emoji/v1/slack/callback"
    val slackClientId = System.getenv("SLACK_CLIENT_ID")
    val slackClientSecret = System.getenv("SLACK_CLIENT_SECRET")

    val args = mutableListOf(
        "docker", "run", "-d",
        "--name", containerName,
        "--network", networkName,
        "-p", "13084:13084",
        "-e", "SPRING_PROFILES_ACTIVE=local",
        "-e", "SLACK_REDIRECT_URI=$slackRedirectUri"
    )

    if (!slackClientId.isNullOrBlank()) {
        args.addAll(listOf("-e", "SLACK_CLIENT_ID=$slackClientId"))
    }
    if (!slackClientSecret.isNullOrBlank()) {
        args.addAll(listOf("-e", "SLACK_CLIENT_SECRET=$slackClientSecret"))
    }

    args.add(imageName)
    commandLine(args)
}

tasks.register<Exec>("dockerStop") {
    group = "docker"
    description = "Stop $serviceName container"
    isIgnoreExitValue = true
    commandLine("docker", "stop", containerName)
}

tasks.register<Exec>("dockerRemove") {
    group = "docker"
    description = "Remove $serviceName container"
    dependsOn("dockerStop")
    isIgnoreExitValue = true
    commandLine("docker", "rm", containerName)
}

tasks.register<Exec>("dockerRestart") {
    group = "docker"
    description = "Restart $serviceName container"
    dependsOn("dockerStop", "dockerRemove", "dockerRun")
}

tasks.register<Exec>("dockerLogs") {
    group = "docker"
    description = "Show logs for $serviceName"
    commandLine("docker", "logs", "-f", containerName)
}

tasks.register<Exec>("dockerClean") {
    group = "docker"
    description = "Clean up $serviceName container and image"
    dependsOn("dockerStop", "dockerRemove")
    doLast {
        exec {
            isIgnoreExitValue = true
            commandLine("docker", "rmi", imageName)
        }
    }
}

// Compose-based tasks for consistency with other services
tasks.register<Exec>("dockerComposeDownSlackEmojiOnly") {
    group = "docker"
    description = "docker compose down (slack-emoji only)"
    isIgnoreExitValue = true
    commandLine("docker", "compose", "-f", "docker-compose.slack-emoji-only.yml", "down")
}

tasks.register<Exec>("dockerComposeUpSlackEmojiOnly") {
    group = "docker"
    description = "docker compose up -d (slack-emoji only)"
    dependsOn("dockerBuild")
    commandLine("docker", "compose", "-f", "docker-compose.slack-emoji-only.yml", "up", "-d")
}

tasks.register("dockerRebuildAndRunSlackEmojiOnly") {
    group = "docker"
    description = "Rebuild and run only $serviceName (with tests, via compose)"

    dependsOn("clean")
    dependsOn("build")
    dependsOn("dockerComposeDownSlackEmojiOnly")
    dependsOn("dockerBuild")
    dependsOn("dockerComposeUpSlackEmojiOnly")

    tasks["build"].mustRunAfter("clean")
    tasks["dockerComposeDownSlackEmojiOnly"].mustRunAfter("build")
    tasks["dockerBuild"].mustRunAfter("dockerComposeDownSlackEmojiOnly")
    tasks["dockerComposeUpSlackEmojiOnly"].mustRunAfter("dockerBuild")
}

tasks.register("dockerQuickRebuildSlackEmojiOnly") {
    group = "docker"
    description = "Quick rebuild and run $serviceName (skip tests, via compose)"

    doFirst {
        println("⚠️  WARNING: Skipping tests - use only for development!")
    }

    dependsOn("clean")
    dependsOn("assemble")
    dependsOn("dockerComposeDownSlackEmojiOnly")
    dependsOn("dockerBuild")
    dependsOn("dockerComposeUpSlackEmojiOnly")

    tasks["assemble"].mustRunAfter("clean")
    tasks["dockerComposeDownSlackEmojiOnly"].mustRunAfter("assemble")
    tasks["dockerBuild"].mustRunAfter("dockerComposeDownSlackEmojiOnly")
    tasks["dockerComposeUpSlackEmojiOnly"].mustRunAfter("dockerBuild")
}

tasks.register<Exec>("dockerStatus") {
    group = "docker"
    description = "Check status of $serviceName container"
    commandLine("docker", "ps", "-a", "--filter", "name=$containerName")
}

tasks.register<Exec>("dockerExec") {
    group = "docker"
    description = "Execute bash shell in $serviceName container"
    commandLine("docker", "exec", "-it", containerName, "/bin/bash")
}

tasks.register<Exec>("dockerInspect") {
    group = "docker"
    description = "Inspect $serviceName container"
    commandLine("docker", "inspect", containerName)
}

tasks.register("dockerBuildAndRunAll") {
    group = "docker"
    description = "Build and run all services including dependencies"
    
    doLast {
        println("=====================================")
        println("Building and running all services...")
        println("=====================================")
        
        exec {
            workingDir = file("../core-platform")
            commandLine("./gradlew", "dockerComposeUp")
        }
        
        Thread.sleep(10000) // Wait for infrastructure
        
        exec {
            commandLine("./gradlew", "dockerRebuildAndRunSlackEmojiOnly")
        }
    }
}

tasks.register("dockerHealth") {
    group = "docker"
    description = "Check health of $serviceName"
    
    doLast {
        exec {
            commandLine("curl", "-f", "http://localhost:13084/actuator/health")
        }
    }
}