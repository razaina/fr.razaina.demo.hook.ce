/*
 *  Collin's Dynamic Dalvik Instrumentation Toolkit for Android
 *  Collin Mulliner <collin[at]mulliner.org>
 *
 *  (c) 2012,2013
 *
 *  License: LGPL v2.1
 *
 */

#define _GNU_SOURCE
#include <stdio.h>
#include <sys/types.h>
#include <sys/stat.h>
#include <fcntl.h>
#include <dlfcn.h>
#include <sys/socket.h>
#include <netinet/in.h>
#include <sys/select.h>
#include <string.h>
#include <termios.h>
#include <pthread.h>
#include <sys/epoll.h>

#include <jni.h>
#include <stdlib.h>

#include "dexstuff.h"
#include "hook.h"
#include "dalvik_hook.h"
#include "base.h"


#include <android/log.h>

#undef log
#define MLOG_TAG "demohookce"

#define log(...) \
		 __android_log_print(ANDROID_LOG_INFO,MLOG_TAG,##__VA_ARGS__)

static struct hook_t eph;
static struct dexstuff_t d;

static int debug;

static void my_log(char *msg)
{
	log("%s",msg);
}

static void my_log2(char *msg)
{
	if (debug)
		log("%s",msg);
}


// helper function
void printString(JNIEnv *env, jobject str, char *l)
{
	if(!str){
		log("%sNULL",l);
		return;
	}
	char *s = (*env)->GetStringUTFChars(env, str, 0);
	if (s) {
		log("%s%s\n", l, s);
		(*env)->ReleaseStringUTFChars(env, str, s); 
	}
}

void writeString(JNIEnv *env, jobject str,char *l, char *res,size_t size)
{
	if(!str){
		snprintf(res,size,"null");
		return;
	}

	char *s = (*env)->GetStringUTFChars(env, str, 0);
	if (s) {
		log("%s%s\n", l, s);
		snprintf(res,size,"%s%s",l,s);

		(*env)->ReleaseStringUTFChars(env, str, s); 
	}
}

void printObj(JNIEnv *env,jobject obj,char *class,char *l)
{
	// Call a java method which returns a string.
	log("trying to print %s obj\n",class);
	jclass cls=(*env)->FindClass(env,class);
	if(!cls)
		return;

	jmethodID midToString = (*env)->GetMethodID(env,cls, "toString", "()Ljava/lang/String;"); 
	if (!midToString){
		log("cannot get toString function in [%s]...\n",class);
		return;
	}
	
	jstring jstr = (jstring) (*env)->CallObjectMethod(env,obj, midToString); 
	if (jstr != 0) { 
		printString(env,jstr,l);
		(*env)->DeleteLocalRef(env,jstr); 
	}else{
		log("cannot get toString method for %s\n",class);
	}
}

void writeObj(JNIEnv *env,jobject obj,char *class,char *l,char *result,size_t size)
{
	// Call a java method which returns a string.
	log("trying to write %s obj\n",class);
	jclass cls=(*env)->FindClass(env,class);
	if(!cls)
		return;

	jmethodID midToString = (*env)->GetMethodID(env,cls, "toString", "()Ljava/lang/String;"); 
	if (!midToString){
		log("cannot get toString function in [%s]...\n",class);
		return;
	}

	jstring jstr = (jstring) (*env)->CallObjectMethod(env,obj, midToString); 
	if (jstr != 0) { 
		writeString(env,jstr,l,result,size);
		(*env)->DeleteLocalRef(env,jstr); 
	}else{
		log("cannot get toString method for %s\n",class);
	}
}



// hooks
#include "hookce.c"

int do_patch()
{
	log("do_patch()\n");
	if(init_hook_init()){
		return 1;
	}

	log("done patching()\n");
	return 0;
}

#include <pthread.h>
pthread_t thread_id;

static void* thread_entry(void *args)
{
	// resolve symbols from DVM
	dexstuff_resolv_dvm(&d);

	while(do_patch()){
		log("Failed to patch...trying again...");
		sleep(1);
	}
	log("Patch succesfull!");
}

static int my_epoll_wait(int epfd, struct epoll_event *events, int maxevents, int timeout)
{
	log(__PRETTY_FUNCTION__);
	int (*orig_epoll_wait)(int epfd, struct epoll_event *events, int maxevents, int timeout);
	orig_epoll_wait = (void*)eph.orig;
	// remove hook for epoll_wait
	hook_precall(&eph);

	if(pthread_create(&thread_id,NULL,thread_entry,NULL)){
		log("[-] pthread_create failed");
	}

	// insert hooks
	//
	
	// call dump class (demo)
	//dalvik_dump_class(&d,"Lcom/google/android/finsky/billing/iab/InAppBillingService;");
        
	// call original function
	int res = orig_epoll_wait(epfd, events, maxevents, timeout);    
	
	
	return res;
}

// set my_init as the entry point
void __attribute__ ((constructor)) my_init(void);

void my_init(void)
{
	log("HookCE: started\n");
 
 	// set to 1 to turn on, this will be noisy
	debug = 1;

 	// set log function for  libbase (very important!)
	set_logfunction(my_log2);
	// set log function for libdalvikhook (very important!)
	dalvikhook_set_logfunction(my_log2);

    hook(&eph, getpid(), "libc.", "epoll_wait", my_epoll_wait, 0);
}



