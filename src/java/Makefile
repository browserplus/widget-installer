BUILD_PATH ?= build/
SOURCE_PATH ?= src/
RAW_SOURCES = $(shell cd src && find com -name "*.java")
SOURCES = $(addprefix src/,$(RAW_SOURCES))
CLASSES = $(addprefix $(BUILD_PATH), $(patsubst %.java,%.class,$(RAW_SOURCES)))
CLASS_PATH = $(BUILD_PATH)$(dir $(shell cd src && find com -name "*.java" | head -1))
CLASS_GLOB = *.class
CHECKJARFILE = $(BUILD_PATH)bp_java_check.jar
FULLJARFILE = $(BUILD_PATH)bp_installer.jar
HAVE_KEYS := $(strip $(shell keytool -list -alias $$USER -storepass browserplus 2>&1 | grep Exception))

.PHONY: all
all: signing_key $(BUILD_PATH) $(CHECKJARFILE) $(FULLJARFILE)

$(FULLJARFILE): $(CLASSES)
	@jar -cvf $@ -C $(BUILD_PATH) com
	@jarsigner -storepass browserplus $@ $$USER

$(CHECKJARFILE): $(BUILD_PATH) $(FULLJARFILE)	@jar -cvf $(CHECKJARFILE) -C $(BUILD_PATH) `cd $(BUILD_PATH) && find . -type f -name javatest.class`

.PHONY: signing_key
signing_key: 
ifeq ("$(HAVE_KEYS)", "")
	@echo "--> Cert for $${USER} already exists"
else
	@echo "--> Generating self signed signing cert for $${USER}"
	@keytool -genkey -alias $$USER -storepass browserplus -keypass browserplus -dname "CN=$${USER}, OU=BrowserPlus, O=Yahoo!, L=Sunnyvale, S=California, C=US"
endif

$(BUILD_PATH):
	mkdir $(BUILD_PATH)

$(CLASSES): $(BUILD_PATH)%.class : $(SOURCE_PATH)%.java
	@echo compiling java
	@javac -d $(BUILD_PATH) -classpath $(BUILD_PATH) -target 1.2 -g -source 1.3 $(patsubst $(BUILD_PATH)%.class, $(SOURCE_PATH)%.java, $(CLASSES))

.PHONY: clean distclean
clean: 
	@rm -f $(CLASSES)
	@rm -f $(JARFILE)

distclean: clean
	@rm -rf $(BUILD_PATH)
