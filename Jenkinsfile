node {
	checkout scm
	sh './gradlew setupDecompWorkspace clean build -x test'
	archive 'build/libs/*jar'
}
