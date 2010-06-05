BUILD_PATH ?= build/
SOURCES ?= bp-install.css bp-install-logo.png
OUTPUTS = $(addprefix $(BUILD_PATH), $(SOURCES))

all: $(BUILD_PATH) $(OUTPUTS)
tag: all

$(BUILD_PATH):
	mkdir $(BUILD_PATH)

$(OUTPUTS): $(BUILD_PATH)% : %
	ln -s $(abspath $^) $@

.PHONY: clean distclean
clean: 
	@rm -f $(OUTPUTS)

distclean: clean
