export BUILD_PATH = $(abspath build)/

SUBDIRS = src/js src/java

all: subdirs
ifdef TAG
	git tag $(TAG)
	@cd $(BUILD_PATH) && tar chzvf widget_installer_$(TAG).tgz bp_installer_signed.jar bp_java_check.jar bp-install-lib.js bp-install.js bp-install.css bp-install-logo.png
endif

.PHONY: subdirs $(SUBDIRS) 
subdirs: $(SUBDIRS)

$(SUBDIRS):
ifdef TAG
	@$(MAKE) -C $@ tag
else
	@$(MAKE) -C $@
endif

.PHONY: clean
clean:
	@rm -rf build
