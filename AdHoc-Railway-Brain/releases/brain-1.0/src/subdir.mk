################################################################################
# Automatically-generated file. Do not edit!
################################################################################

# Add inputs and outputs from these tool invocations to the build variables 
C_SRCS += \
../src/booster.c \
../src/debug.c \
../src/fifo.c \
../src/ib_parser.c \
../src/main.c \
../src/pwm.c \
../src/spi.c \
../src/uart_interrupt.c 

OBJS += \
./src/booster.o \
./src/debug.o \
./src/fifo.o \
./src/ib_parser.o \
./src/main.o \
./src/pwm.o \
./src/spi.o \
./src/uart_interrupt.o 

C_DEPS += \
./src/booster.d \
./src/debug.d \
./src/fifo.d \
./src/ib_parser.d \
./src/main.d \
./src/pwm.d \
./src/spi.d \
./src/uart_interrupt.d 


# Each subdirectory must supply rules for building sources it contributes
src/%.o: ../src/%.c
	@echo 'Building file: $<'
	@echo 'Invoking: AVR Compiler'
	avr-gcc -Wall -Os -fpack-struct -fshort-enums -ffunction-sections -fdata-sections -std=gnu99 -funsigned-char -funsigned-bitfields -mmcu=atmega644 -DF_CPU=20000000UL -MMD -MP -MF"$(@:%.o=%.d)" -MT"$(@:%.o=%.d)" -c -o "$@" "$<"
	@echo 'Finished building: $<'
	@echo ' '


