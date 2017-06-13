node {
	checkout scm
	sh './gradlew setupDecompWorkspace clean build'
	archive 'build/libs/*jar'
}
