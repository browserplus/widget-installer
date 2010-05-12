export BUILD_PATH = $(abspath build)/

SUBDIRS = src/js src/java

all: subdirs

.PHONY: subdirs $(SUBDIRS) 
subdirs: $(SUBDIRS)

$(SUBDIRS):
	@$(MAKE) -C $@

.PHONY: clean
clean:
	@rm -rf build
