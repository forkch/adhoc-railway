################################################################################
# Automatically-generated file. Do not edit!
################################################################################

-include ../makefile.init

RM := rm -rf

# All of the sources participating in the build are defined here
-include sources.mk
-include src/subdir.mk
-include subdir.mk
-include objects.mk

ifneq ($(MAKECMDGOALS),clean)
ifneq ($(strip $(C_DEPS)),)
-include $(C_DEPS)
endif
ifneq ($(strip $(ASM_DEPS)),)
-include $(ASM_DEPS)
endif
ifneq ($(strip $(S_DEPS)),)
-include $(S_DEPS)
endif
ifneq ($(strip $(S_UPPER_DEPS)),)
-include $(S_UPPER_DEPS)
endif
endif

-include ../makefile.defs

# Add inputs and outputs from these tool invocations to the build variables 
LSS += \
AdHoc-Railway-Brain.lss \

FLASH_IMAGE += \
AdHoc-Railway-Brain.hex \

EEPROM_IMAGE += \
AdHoc-Railway-Brain.eep \

SIZEDUMMY += \
sizedummy \

AVRDUDEDUMMY += \
avrdudedummy \


# All Target
all: AdHoc-Railway-Brain.elf secondary-outputs

# Tool invocations
AdHoc-Railway-Brain.elf: $(OBJS) $(USER_OBJS)
	@echo 'Building target: $@'
	@echo 'Invoking: AVR C Linker'
	avr-gcc -Wl,-Map,AdHoc-Railway-Brain.map -mmcu=atmega644 -o "AdHoc-Railway-Brain.elf" $(OBJS) $(USER_OBJS) $(LIBS)
	@echo 'Finished building target: $@'
	@echo ' '

AdHoc-Railway-Brain.lss: AdHoc-Railway-Brain.elf
	@echo 'Invoking: AVR Create Extended Listing'
	-avr-objdump -h -S AdHoc-Railway-Brain.elf  >"AdHoc-Railway-Brain.lss"
	@echo 'Finished building: $@'
	@echo ' '

AdHoc-Railway-Brain.hex: AdHoc-Railway-Brain.elf
	@echo 'Create Flash image (ihex format)'
	-avr-objcopy -R .eeprom -R .fuse -R .lock -R .signature -O ihex AdHoc-Railway-Brain.elf  "AdHoc-Railway-Brain.hex"
	@echo 'Finished building: $@'
	@echo ' '

AdHoc-Railway-Brain.eep: AdHoc-Railway-Brain.elf
	@echo 'Create eeprom image (ihex format)'
	-avr-objcopy -j .eeprom --no-change-warnings --change-section-lma .eeprom=0 -O ihex AdHoc-Railway-Brain.elf  "AdHoc-Railway-Brain.eep"
	@echo 'Finished building: $@'
	@echo ' '

sizedummy: AdHoc-Railway-Brain.elf
	@echo 'Invoking: Print Size'
	-avr-size --format=avr --mcu=atmega644 AdHoc-Railway-Brain.elf
	@echo 'Finished building: $@'
	@echo ' '

avrdudedummy: AdHoc-Railway-Brain.elf
	@echo 'Invoking: AVRDude'
	/usr/bin/avrdude -pm644 -cusbasp -Pusb -Uflash:w:AdHoc-Railway-Brain.hex:a
	@echo 'Finished building: $@'
	@echo ' '

# Other Targets
clean:
	-$(RM) $(OBJS)$(C_DEPS)$(ASM_DEPS)$(EEPROM_IMAGE)$(FLASH_IMAGE)$(ELFS)$(LSS)$(AVRDUDEDUMMY)$(S_DEPS)$(SIZEDUMMY)$(S_UPPER_DEPS) AdHoc-Railway-Brain.elf
	-@echo ' '

secondary-outputs: $(LSS) $(FLASH_IMAGE) $(EEPROM_IMAGE) $(SIZEDUMMY) $(AVRDUDEDUMMY)

.PHONY: all clean dependents
.SECONDARY:

-include ../makefile.targets
