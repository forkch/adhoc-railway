################################################################################
# Automatically-generated file. Do not edit!
################################################################################

-include ../makefile.init

RM := rm -rf

# All of the sources participating in the build are defined here
-include sources.mk
-include subdir.mk
-include src/subdir.mk
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
AdHoc-MaerklinGenerator.lss \

FLASH_IMAGE += \
AdHoc-MaerklinGenerator.hex \

EEPROM_IMAGE += \
AdHoc-MaerklinGenerator.eep \

SIZEDUMMY += \
sizedummy \

AVRDUDEDUMMY += \
avrdudedummy \


# All Target
all: AdHoc-MaerklinGenerator.elf secondary-outputs

# Tool invocations
AdHoc-MaerklinGenerator.elf: $(OBJS) $(USER_OBJS)
	@echo 'Building target: $@'
	@echo 'Invoking: AVR C Linker'
	avr-gcc -Wl,-Map,AdHoc-MaerklinGenerator.map -mmcu=atmega8 -o "AdHoc-MaerklinGenerator.elf" $(OBJS) $(USER_OBJS) $(LIBS)
	@echo 'Finished building target: $@'
	@echo ' '

AdHoc-MaerklinGenerator.lss: AdHoc-MaerklinGenerator.elf
	@echo 'Invoking: AVR Create Extended Listing'
	-avr-objdump -h -S AdHoc-MaerklinGenerator.elf  >"AdHoc-MaerklinGenerator.lss"
	@echo 'Finished building: $@'
	@echo ' '

AdHoc-MaerklinGenerator.hex: AdHoc-MaerklinGenerator.elf
	@echo 'Create Flash image (ihex format)'
	-avr-objcopy -R .eeprom -O ihex AdHoc-MaerklinGenerator.elf  "AdHoc-MaerklinGenerator.hex"
	@echo 'Finished building: $@'
	@echo ' '

AdHoc-MaerklinGenerator.eep: AdHoc-MaerklinGenerator.elf
	@echo 'Create eeprom image (ihex format)'
	-avr-objcopy -j .eeprom --no-change-warnings --change-section-lma .eeprom=0 -O ihex AdHoc-MaerklinGenerator.elf  "AdHoc-MaerklinGenerator.eep"
	@echo 'Finished building: $@'
	@echo ' '

sizedummy: AdHoc-MaerklinGenerator.elf
	@echo 'Invoking: Print Size'
	-avr-size --format=avr --mcu=atmega8 AdHoc-MaerklinGenerator.elf
	@echo 'Finished building: $@'
	@echo ' '

avrdudedummy: AdHoc-MaerklinGenerator.elf
	@echo 'Invoking: AVRDude'
	C:\WinAVR-20100110\bin\avrdude -pm8 -u -Uflash:w:AdHoc-MaerklinGenerator.hex:a -Ulfuse:w:0xe0:m -Uhfuse:w:0xd9:m
	@echo 'Finished building: $@'
	@echo ' '

# Other Targets
clean:
	-$(RM) $(OBJS)$(C_DEPS)$(ASM_DEPS)$(EEPROM_IMAGE)$(FLASH_IMAGE)$(ELFS)$(LSS)$(AVRDUDEDUMMY)$(S_DEPS)$(SIZEDUMMY)$(S_UPPER_DEPS) AdHoc-MaerklinGenerator.elf
	-@echo ' '

secondary-outputs: $(LSS) $(FLASH_IMAGE) $(EEPROM_IMAGE) $(SIZEDUMMY) $(AVRDUDEDUMMY)

.PHONY: all clean dependents
.SECONDARY:

-include ../makefile.targets
