#include <Carbon/Carbon.h>
#include "jni.h"

static int MAX_PROCESSES = 255;

char *getActiveProcess()
{
	Str255 processName;
	struct ProcessInfoRec info = {
			.processInfoLength = sizeof(struct ProcessInfoRec),
			.processName = processName
		};
	ProcessSerialNumber interrupted_process;

	GetFrontProcess( &interrupted_process );
	GetProcessInformation(&interrupted_process, &info);

	//Turn the Pascal string into a C-string.
	size_t length = processName[0];
	memmove(processName, processName + 1, length);
	processName[length] = '\0';

	char *r = strdup((const char *)processName);
	return(r);

}

void getAllProcesses(char *ret[],int *refIndex)
{
	OSStatus err;
	int index = 0;

	struct ProcessSerialNumber psn = { 0, 0 };
	while ((err = GetNextProcess(&psn)) == noErr) {
		Str255 processName;
		struct ProcessInfoRec info = {
			.processInfoLength = sizeof(struct ProcessInfoRec),
			.processName = processName
		};
		GetProcessInformation(&psn, &info);

		//Turn the Pascal string into a C-string.
		size_t length = processName[0];
		memmove(processName, processName + 1, length);
		processName[length] = 0;

		ret[index] = strdup((const char *)processName);
		index++;
		if(index == MAX_PROCESSES){
			index = MAX_PROCESSES-1;
			fprintf(stderr, "Too many processes, dropping: %s\n", ret[index]);
		}
	}

	if(err != procNotFound) {
		fprintf(stderr, "GetNextProcess returned error: %i\n", (int)err);
	}
	*refIndex = index;
}

/*
 * Class:     edu_uci_ics_luci_cacophony_sensors_ProcessMac
 * Method:    sampleActiveProcess
 * Signature: ()Ljava/lang/String;
 */
JNIEXPORT jstring JNICALL Java_edu_uci_ics_luci_cacophony_sensors_ProcessMac_sampleActiveProcess
  (JNIEnv *env, jclass ob)
{
	fprintf(stderr,"native code: process: sampleActiveProcess\n");

	char *active = getActiveProcess();

	if(active != NULL){
		jstring processString = (*env)->NewStringUTF(env, (char*)active);
		free(active);
		return processString;
	}
	else{
		return NULL;
	}

}

/*
 * Class:     edu_uci_ics_luci_cacophony_sensors_ProcessMac
 * Method:    sampleAllProcesses
 * Signature: ()[Ljava/lang/String;
 */
JNIEXPORT jobjectArray JNICALL Java_edu_uci_ics_luci_cacophony_sensors_ProcessMac_sampleAllProcesses
  (JNIEnv *env, jclass ob)
{
	fprintf(stderr,"native code: process: sampleAllProcesses\n");

	jobjectArray results = (*env)->NewObjectArray(env, MAX_PROCESSES, (*env)->FindClass(env, "java/lang/String"), 0);

	int index;
	char * processes[MAX_PROCESSES];

	getAllProcesses(processes,&index);

	int i;
	for(i = 0; i < index; i++){
		if(processes[i] != NULL){
			(*env)->SetObjectArrayElement(env, results, i, (*env)->NewStringUTF( env, processes[i] ));
			free(processes[i]);
		}
		else{
			(*env)->SetObjectArrayElement(env, results, i, NULL);
		}
	}

	return(results);
}


int main (int argc, char **argv) {
	char *active = getActiveProcess();
	printf("Active Process: \n\t%s\n",active);
	free(active);

	printf("Other Processes:\n");

	int index;
	char * processes[MAX_PROCESSES];
	getAllProcesses(processes,&index);
	int i;
	for(i = 0; i < index; i++){
		printf("\t %s\n", processes[i]);
		free(processes[i]);
	}
	return EXIT_SUCCESS;
}
