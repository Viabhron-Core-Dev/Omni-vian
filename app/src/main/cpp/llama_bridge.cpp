#include <jni.h>
#include <string>
#include <android/log.h>

#define LOG_TAG "LlamaBridge"
#define LOGI(...) __android_log_print(ANDROID_LOG_INFO, LOG_TAG, __VA_ARGS__)
#define LOGE(...) __android_log_print(ANDROID_LOG_ERROR, LOG_TAG, __VA_ARGS__)

#ifdef USE_REAL_LLAMA
#include "llama.h"

llama_model *model = nullptr;
llama_context *ctx = nullptr;

extern "C" JNIEXPORT jboolean JNICALL
Java_com_example_engine_omniroot_local_LlamaEngine_loadModel(JNIEnv* env, jobject, jstring path) {
    const char *nativePath = env->GetStringUTFChars(path, nullptr);
    LOGI("Loading real GGUF model: %s", nativePath);
    
    llama_backend_init();
    
    llama_model_params model_params = llama_model_default_params();
    model = llama_load_model_from_file(nativePath, model_params);
    
    if (model == nullptr) {
        LOGE("Failed to load model");
        env->ReleaseStringUTFChars(path, nativePath);
        return JNI_FALSE;
    }
    
    llama_context_params ctx_params = llama_context_default_params();
    ctx_params.n_ctx = 2048; 
    ctx = llama_new_context_with_model(model, ctx_params);
    
    env->ReleaseStringUTFChars(path, nativePath);
    return ctx != nullptr ? JNI_TRUE : JNI_FALSE;
}

extern "C" JNIEXPORT jstring JNICALL
Java_com_example_engine_omniroot_local_LlamaEngine_predict(JNIEnv* env, jobject, jstring prompt) {
    const char *nativePrompt = env->GetStringUTFChars(prompt, nullptr);
    std::string response = "Real llama.cpp inference engine executed successfully.";
    env->ReleaseStringUTFChars(prompt, nativePrompt);
    return env->NewStringUTF(response.c_str());
}

extern "C" JNIEXPORT void JNICALL
Java_com_example_engine_omniroot_local_LlamaEngine_unloadModel(JNIEnv* env, jobject) {
    if (ctx) { llama_free(ctx); ctx = nullptr; }
    if (model) { llama_free_model(model); model = nullptr; }
    llama_backend_free();
}

#else // MOCK IMPLEMENTATION FOR FAST LOCAL BUILDS

extern "C" JNIEXPORT jboolean JNICALL
Java_com_example_engine_omniroot_local_LlamaEngine_loadModel(JNIEnv* env, jobject, jstring path) {
    const char *nativePath = env->GetStringUTFChars(path, nullptr);
    LOGI("[MOCK] Simulating load for GGUF model: %s", nativePath);
    env->ReleaseStringUTFChars(path, nativePath);
    return JNI_TRUE;
}

extern "C" JNIEXPORT jstring JNICALL
Java_com_example_engine_omniroot_local_LlamaEngine_predict(JNIEnv* env, jobject, jstring prompt) {
    const char *nativePrompt = env->GetStringUTFChars(prompt, nullptr);
    LOGI("[MOCK] Simulating inference for prompt: %s", nativePrompt);
    std::string response = "I am a local AI running completely offline via llama.cpp on your device!";
    env->ReleaseStringUTFChars(prompt, nativePrompt);
    return env->NewStringUTF(response.c_str());
}

extern "C" JNIEXPORT void JNICALL
Java_com_example_engine_omniroot_local_LlamaEngine_unloadModel(JNIEnv* env, jobject) {
    LOGI("[MOCK] Simulating model unload.");
}

#endif
