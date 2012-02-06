################################################################################
# Automatically-generated file. Do not edit!
################################################################################

# Add inputs and outputs from these tool invocations to the build variables 
C_SRCS += \
../src/debug.c \
../src/fifo.c \
../src/main.c \
../src/pwm.c \
../src/spi.c \
../src/uart.c \
../src/uart_interrupt.c 

OBJS += \
./src/debug.o \
./src/fifo.o \
./src/main.o \
./src/pwm.o \
./src/spi.o \
./src/uart.o \
./src/uart_interrupt.o 

C_DEPS += \
./src/debug.d \
./src/fifo.d \
./src/main.d \
./src/pwm.d \
./src/spi.d \
./src/uart.d \
./src/uart_interrupt.d 


# Each subdirectory must supply rules for building sources it contributes
src/%.o: ../src/%.c
	@echo 'Building file: $<'
	@echo 'Invoking: AVR Compiler'
	avr-gcc -Wall -g2 -gstabs -O2 -fpack-struct -fshort-enums -std=gnu99 -funsigned-char -funsigned-bitfields -mmcu=atmega8 -DF_CPU=16000000UL -MMD -MP -MF"$(@:%.o=%.d)" -MT"$(@:%.o=%.d)" -c -o "$@" "$<"
	@echo 'Finished building: $<'
	@echo ' '


