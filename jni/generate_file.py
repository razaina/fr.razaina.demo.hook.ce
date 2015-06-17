#!/bin/python

#static struct dalvik_hook_t hook_callIDE;
#static jobject fn_callIDE(JNIEnv *env, jobject obj, jobject listener, jobject username, jobject password)
#{
#        
#        
#        
#}


#dalvik_hook_setup(&hook_callIDE,
#    "Lcom/e_i/bad/controls/fragments/ident/IdentificationFragment;",
#    "callIDE",
#    "(Lcom/e_i/bad/webservices/listener/ident/IDEListener;Ljava/lang/String;Ljava/lang/String;)V",4,
#    fn_callIDE);
#
#hook_callIDE.debug_me=1;
#if(NULL==dalvik_hook(&d,&hook_callIDE)){
#	return 1;
#}


def generate_stubs(className,prototype,function,num_arg):
    print("static struct dalvik_hook_t hook_"+function+";");

    arguments=""
    arglist=""
    for i in range(0,num_arg):
        arguments+=", jobject arg%d"%i
        arglist+=",arg%d"%i

    #template method
    print("static jobject fn_%s(JNIEnv *env,jobject obj"%function+arguments+")")
    print("{")
    for i in range(3):
        print("\t//TODO")
    print("")
    
    print("\tdalvik_prepare(&d,&hook_"+function+",env);")
    print("\tjobject res = (*env)->CallObjectMethod(env,obj,hook_%s.mid%s);"%(function,arglist))
    print("\tdalvik_postcall(&d,&hook_"+function+");")
    print("\treturn res;")
    print("}")

    #Initialization
    print("static int init_hook_"+function+"()")
    print("{")
    print("\tdalvik_hook_setup(&hook_"+function+",")
    print("\t\t\""+className+"\",")
    print("\t\t\""+function+"\",")
    print("\t\t\""+prototype+"\",%d,"%(num_arg+1))
    print("\t\tfn_"+function+");")

    print("\thook_"+function+".debug_me=1;")

    print("\tif (NULL == dalvik_hook(&d,&hook_%s)){"%function)
    print("\t\tdalvik_dump_class(&d,\""+className+"\");")
    print("\t\treturn 1;")
    print("\t}")

    print("\treturn 0;")
    print("}")

generate_stubs("Lcom.caisseepargne.android.mobilebanking.commons.c.a.p;",
               "(Landroid/os/Handler;Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;)V",
               "init",
               6)
