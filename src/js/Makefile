BUILD_PATH ?= build/
SOURCES ?= bp-install-lib.js bp-install-ui.js
OUTPUTS = $(addprefix $(BUILD_PATH), $(SOURCES))
COMBO=$(addprefix $(BUILD_PATH), install.js)
COMBO_MIN=$(addprefix $(BUILD_PATH), install-min.js)

all: $(BUILD_PATH) $(OUTPUTS) $(COMBO)

tag: all

$(BUILD_PATH):
	mkdir $(BUILD_PATH)

$(OUTPUTS): $(BUILD_PATH)% : %
	ln -s $(abspath $^) $@

$(COMBO): $(SOURCES)
	cat $(SOURCES) > $@
	yuicompressor $(COMBO) > $(COMBO_MIN)

.PHONY: clean distclean
clean:
	@rm -rf $(BUILD_PATH)

distclean: clean
