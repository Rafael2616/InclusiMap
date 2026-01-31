Pod::Spec.new do |spec|
    spec.name                     = 'map'
    spec.version                  = '1.0'
    spec.homepage                 = 'No links'
    spec.source                   = { :http=> ''}
    spec.authors                  = ''
    spec.license                  = ''
    spec.summary                  = 'Inclusimap map module'
    spec.vendored_frameworks      = 'build/cocoapods/framework/map.framework'
    spec.libraries                = 'c++'
    spec.ios.deployment_target    = '18.2'
    spec.dependency 'GoogleMaps', '10.0.0'
    if !Dir.exist?('build/cocoapods/framework/map.framework') || Dir.empty?('build/cocoapods/framework/map.framework')
        raise "
        Kotlin framework 'map' doesn't exist yet, so a proper Xcode project can't be generated.
        'pod install' should be executed after running ':generateDummyFramework' Gradle task:
            ./gradlew :feature:map:generateDummyFramework
        Alternatively, proper pod installation is performed during Gradle sync in the IDE (if Podfile location is set)"
    end
    spec.xcconfig = {
        'ENABLE_USER_SCRIPT_SANDBOXING' => 'NO',
    }
    spec.pod_target_xcconfig = {
        'KOTLIN_PROJECT_PATH' => ':feature:map',
        'PRODUCT_MODULE_NAME' => 'map',
    }
    spec.script_phases = [
        {
            :name => 'Build map',
            :execution_position => :before_compile,
            :shell_path => '/bin/sh',
            :script => <<-SCRIPT
                if [ "YES" = "$OVERRIDE_KOTLIN_BUILD_IDE_SUPPORTED" ]; then
                    echo "Skipping Gradle build task invocation due to OVERRIDE_KOTLIN_BUILD_IDE_SUPPORTED environment variable set to \"YES\""
                    exit 0
                fi
                set -ev
                REPO_ROOT="$PODS_TARGET_SRCROOT"
                "$REPO_ROOT/../../gradlew" -p "$REPO_ROOT" $KOTLIN_PROJECT_PATH:syncFramework \
                    -Pkotlin.native.cocoapods.platform=$PLATFORM_NAME \
                    -Pkotlin.native.cocoapods.archs="$ARCHS" \
                    -Pkotlin.native.cocoapods.configuration="$CONFIGURATION"
            SCRIPT
        }
    ]
    spec.resources = ['build\compose\cocoapods\compose-resources']
end
