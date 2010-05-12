BUILD_PATH ?= build/
SOURCE_PATH ?= src/
RAW_SOURCES = $(shell cd src && find com -name "*.java")
SOURCES = $(addprefix src/,$(RAW_SOURCES))
CLASSES = $(addprefix $(BUILD_PATH), $(patsubst %.java,%.class,$(RAW_SOURCES)))
CLASS_PATH = $(BUILD_PATH)$(dir $(shell cd src && find com -name "*.java" | head -1))
CLASS_GLOB = *.class
JARFILE = $(BUILD_PATH)bp_installer.jar

all: $(JARFILE)

$(JARFILE): $(BUILD_PATH) $(CLASSES)
	jar -cvf $(JARFILE) -C $(BUILD_PATH) com

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
