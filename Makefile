export BUILD_PATH = $(abspath build)/

SUBDIRS = src/js src/java
ifeq ($(TAG), "")
   TARGET = ""
else
   TARGET = "tag"
endif

all: subdirs
ifeq ($(TARGET), "tag")
	git tag $(TAG)
	@cd $(BUILD_PATH) && tar czvf widget_installer_$(TAG).tgz bp_installer_signed.jar bp_java_check.jar bpInstallLib.js
endif

.PHONY: subdirs $(SUBDIRS) 
subdirs: $(SUBDIRS)

$(SUBDIRS):
	@$(MAKE) -C $@ $(TARGET)

.PHONY: clean
clean:
	@rm -rf build
