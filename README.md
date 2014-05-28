This project aims to build a compiler for the (yet to be defined) programming language Oak in less than 10.000 lines of Java code. With a focus on simplicity and elegance - not on the performance of the produced assembly (or the pretty error messages).

##Notes on targets, etc.
The intended targets are x86 (with stdclib support) and MIPS (MARS and SPIM dialect) assembler.
For simplicity reasons, no values are kept in the registers except the ones that are required for each operation.

##Development notes
The development is documentation driven (every feature of Oak should first by defined in this document) and semi test driven (every non trivial method of the Code should be tested via a JUnit test).
- The error messages are not pretty and the parser can't recover from syntax errors (like an ANTLR generated one), but that's intended, as it keeps the code base and the level complexity small.

##License
This (and all other texts of this project) are CC-BY licensed.
All code is GNU GPL v3 licensed.

##Roadmap
Things to be done in chronological order.
- lexer (done)
- parser (done)
- semantic analyser
- compiler frontend
- generic compiler backend
- compiler backends for the intended targets
    - x86 (probably with the help of LLVM)
    - MIPS
- (optional) additional compiler backends for targets like
    - MIMA
- (optional) simple AST based interpreter
- (optional) higher level language that compiles to Oak

###Status
####Current work
- Finish testing the parser.
- Start the semantic analyser

The development of this project consumes currently more time, than I can effort. It's therefore almost halting for the next two month. But I'm planning to finish the part "generic compiler backend" in mid august.

##Oak
Oak is a simple language, that can be parsed using only Java's standard library, without the need of a heavy parser generator (like ANTLR) or a PEG parser (like parboiled).


Oak is
- procedural
- static typed
- whitespace insensitive

###Supported Types

name    | size (in bytes) | notable properties           | used for              | declaration syntax (as a regular expression)
--------|----------------:|------------------------------|-----------------------|------------------------------------------
bool    | 1               |                              | boolean arithmetic    | `true¦false`
byte    | 1               | signed                       | strings               | `[+-]?[0-9]+b`
int     | 4               | signed                       | calculations          | `[+-]?[0-9]+`
float   | 4               | IEE-754 floating point       | calculations          | `[+-]?[0-9]+\.[0-9]+(E[+-]?[0-9]+)?`
pointer | 8               | range checks                 |                       | 
string  | 8               | pointer to bytes             |                       | `"([^\\"] ¦ \\" ¦ \\n ¦ \\r ¦ \\t ¦ \\\\)*"`

####Pointer
In general, a pointer references a memory area as bytes. 
A pointer consists of two 4 byte pointers, the first pointing at the first byte and the last pointing at last byte of the referenced memory area. The 4 byte pointers are not accesable to the user.
Invalid pointers consist of two 4 byte pointers, the last 4 byte pointer pointing before the first 4 byte pointer.

###File structure
Statements are separated by new lines (`\n`).
A file consist of several function and global variable declarations.
The `main` function is the entry point into the programm. It's required for code that should be run directly. It's not required for library code.

###Variable declaration and assignment

A variable is declared in the following way:

```
    TYPE NAME = VALUE
```

`TYPE` is the type of the variable (see __Supported Types__).

`NAME` =~ `/[A-Za-z][A-Za-z_0-9]*[!]?/` (`NAME` has the given format, stated as a regular expression.)
Type names (like `int` and `void`) and boolean literals (`true` and `false`) can't be used as a `NAME`. 

`VALUE` is the initial value of the variable (see __Supported Types__) or a name of another variable (declared in this or an outer scope before).
The `VALUE` has to have the type `TYPE`.

A variable can only declared once in a scope and variable declarations of an inner scope hide the declarations of an outer scope.

After a variable is declared, it can be assigned with the following syntax:

```
    NAME = VALUE
```

`NAME` is a previously declared variable (in this or an outer scope).

`VALUE` is the new value of the variable (see __Supported Types__) or a name of another variable (declared in this or an outer scope before) and has the same type as `NAME`.

In general, a variable can only be accessed after it has been declared in the same scope or parent scopes.

Be aware that a memory area referenced by a string or a pointer should be freed after use.

####Global variables
Global variables are declared in the global scope – they can be accessed from every other scope.
Variable assignments and declarations are evaluated from top to down.

###Functions
A function can be declared with the following.


```
    _function RETURN_TYPE NAME : [type of argument 1] [name of argument 1], [...], [type of argument N] [name of argument N]
        [statement_1]
        [...]
        [statement m]
        [return statement]
    _end
```

It declares the function `NAME` with a set of arguments, which returns a value of the type `RETURN_TYPE`. Functions must be declared with a return type but can have zero arguments. The arguments are mapped to variables with the appropriate (given) name inside the function base scope.
`RETURN_TYPE` can also be `void`, meaning that the function doesn't return anything.
The last statement has to be a return statement (omittable if the function has the return type `void`).

A function can also be declared without any arguments:

```
    _function RETURN_TYPE NAME
        [see above]
    _end
```

It's allowed to name two functions the same name if they have different sets of argument types, as well as to name a variable and a function the same. But it's not allowed to use a type name (e.g. `int` or `void`) or a bool literal (`true` or `false`) as a name. 

###Statements

Statements are separated by newlines and are only allowed in function bodies.

####Function calls

```
    NAME [first argument] [...] [last argument]
```

Or without any arguments:

```
    NAME
```
This calls a function `NAME`. The return value is stored in the function local variable `ret_TYPE` with `TYPE` being the return type of the function `NAME` (e.g. the return value of a function returning a bool is stored in the variable `ret_bool`). But this is not the case if the return type is `void`.

The default values of the `ret_TYPE` variables are the following:

variable    | default value
------------|--------------
ret_bool    | false
ret_byte    | 0
ret_int     | 0
ret_float   | +0
ret_string  | ""
ret_pointer | pointer to an empty memory area

####Function return statement
This statement returns from its enclosing function to the calling function.

```
    _return VALUE
```

`VALUE` is either a valid variable name or a value representation (see __Supported Types__) and must have the same type as the return type of its enclosing function.

If the return type is `void`, `VALUE` has to be omitted.

###Control structures (statements)

It's assumed in the following, that `BOOL` is a bool variable or a bool value.

####Conditionals

```
    _if BOOL
        [block of statements executed if BOOL is true]
    _else
        [block of statements executed otherwise]
    _end
```

Or

```
    _if BOOL
        [block of statements executed if BOOL is true]
    _end
```

####While loop

```
    _while BOOL
        [block of statements]
    _end
```

The `BOOL` is checked at the start of each iteration of the loop, the `[block of statements]` is then executed if `BOOL` is true.

###Built in functions
The following are the builtin functions, with their function headers (argument names are omitted, as well as the collon between name and argument list) and a short description.

####Type conversions
- `bool` __to_bool__ `byte`
Converts a byte into a bool, `0` turns into `false`, every other number into `true`.
- `bool` __to_bool__ `int`
Converts an int into a bool, `0` turns into `false`, every other number into `true`.
- `bool` __to_bool__ `float`
Converts an float into a bool, `+0`, `-0` and `NaN` turn into `false`, every other number into `true`.
- `byte` __to_byte__ `bool`
Convert a bool into an int, `true` turns into `1` and `false` to `0`.
- `byte` __to_byte__ `int`
Convert a byte into an int. If the `int` is to large for a `byte` then it throws an exception.
- `int` __to_int__ `bool`
Convert a bool into an int, `true` turns into `1` and `false` to `0`.
- `int` __to_int__ `byte`
Convert a byte into an int.
- `int` __to_int__ `float`
Convert a float into an int, the actual conversion details are target specific and therefore not specified here.
- `float` __to_float__ `int`
Convert a int into a float.
- `int` __round__ `int float`
Rounds the float to the nearest integer and returns it, `-Infinity` becomes `INT_MIN` and `+Infinity` becomes `INT_MAX`.
It throws an exception if the `float` is `NaN`. 
- `int` __floor__ `float`
Returns the biggest integer smaller than the float, `-Infinity` turns into `INT_MIN` and `+Infinity` into `INT_MAX`.
It throws an exception if the `float` is `NaN`. 
- `pointer` __to_pointer__ `string`
Converts a string into an equivalent pointer (that references the bytes of the string).
- `string` __to_string__ `pointer`
Converts a pointer into an equivalent string (treating the pointer as a pointer referencing bytes).
- `string` __to_string__ `byte`
Converts a byte into its string representation, thereby allocating a new string on the heap.
- `string` __to_string__ `int`
Converts an int into its string representation, thereby allocating a new string on the heap.
- `string` __to_string__ `float`
Converts a float into its string representation, thereby allocating a new string on the heap.

####Arithmetic operations
- `byte` __add__ `byte byte`
Calculates the sum of two bytes (overflows can occur).
- `int` __add__ `int int`
Calculates the sum of two ints (overflows can occur).
- `float` __add__ `float float`
Calculates the sum of two floats.

- `byte` __sub__ `byte byte`
Substracts the second byte from the first (overflows can occur).
- `int` __sub__ `int int`
Substracts the second int from the first (overflows can occur).
- `float` __sub__ `float float`
Substracts the second float from the first (over- and underflows can occur).

- `byte` __mul__ `byte byte`
Multiplies the two bytes (overflows can occur).
- `int` __mul__ `int int`
Multiplies the two ints (overflows can occur).
- `float` __mul__ `float float`
Multiplies the two floats (over- and underflows can occur).

- `byte` __div__ `byte byte`
Divides the first byte by the second (over- and underflows can occur). Throws an exception if the divisor is zero.
- `int` __div__ `int int`
Divides the first int by the second (over- and underflows can occur). Throws an exception if the divisor is zero.
- `float` __div__ `float float`
Divides the first float by the second. Throws an exception if the divisor is zero.

- `byte` __mod__ `byte byte`
Calculate `a mod b`. Throws an exception if the second `byte` is zero.
(Uses the mathematical correct version of this operation.)
- `int` __mod__ `int int`
Calculate `a mod b`. Throws an exception if the second `int` is zero.
(Uses the mathematical correct version of this operation.)

####Boolean operations
- `bool` __not__ `bool`
Negates the given bool.
- `bool` __and__ `bool bool`.
Returns `true` if both given bools are `true`, otherwise it returns `false`.
- `bool` __or__ `bool bool`.
Returns `false` if both given bools are `false`, otherwise it returns `true`.
- `bool` __xor__ `bool bool`.
Returns `true` if both given bools have different values, otherwise it returns `true`.

####Compare operations
- `bool` __equal__ `bool bool`
Returns `true` if both bools have the same value, otherwise it returns `false`.
- `bool` __equal__ `byte byte`
Returns `true` if both bytes have the same value, otherwise it returns `false`.
- `bool` __equal__ `int int`
Returns `true` if both ints have the same value, otherwise it returns `false`.
- `bool` __equal__ `float float`
Returns `true` if both floats have exactly the same value, otherwise it returns `false`.
It also returns `false` if one of the `float`s is `NaN`.
`+0` and `-0` are the same.
- `bool` __equal__ `pointer pointer`
Returns `true` if both pointers reference the same memory area, otherwise it returns `false`.
- `bool` __equal__ `string string`
Returns `true` if both strings consist of the same sequence of bytes, otherwise it returns `false`.

- `bool` __less__ `byte byte`
Returns `true` if the first byte is smaller than the second, otherwise it returns `false`.
- `bool` __less__ `int int`
Returns `true` if the first int is smaller than the second, otherwise it returns `false`.
- `bool` __less__ `float float`
Returns `true` if the first float is smaller than the second, otherwise it returns `false`.
It also returns false if one of the `float`s is `NaN` and if one is `+0` and the other `-0`.

####Variable references
Variable references are pointers pointing to a single variable.
Use the following function call statement to get a variable reference (stored in `ret_pointer`) for a variable `VAR`:

```
    get_ref VAR
```
The function `get_ref` is defined for all data types.
`get_ref VAR` is equivalent to `&VAR` in C or C++.

####Pointers and heap allocation
- `pointer` __malloc__ `int`
Allocate a memory area of the given size on the heap and return a referencing pointer.
- `void` __free__ `pointer`
This function takes a reference to a pointer, frees the memory designated to this pointer and invalidates it.
- `pointer` __clone_mem__ `pointer`
Create a copy of the memory area referenced by the pointer and return the pointer to the copy.
- `void` __copy_mem__ `pointer pointer`
Copy the memory referenced by the first pointer to the one referenced by the second pointer.
The last memory area must be at least as big as the first.
- `pointer` __append__ `pointer pointer`
Create a new pointer referencing a memory area with the sum of the sizes of both given pointers as its size and copy the first pointers area to the beginning and copy the second pointers area after it.
- `int` __mem_size__ `pointer`
Returns the size of the memory area referenced by the pointer or `-1` if the pointer is invalid.

- `pointer` __add__ `pointer int`
Returns a pointer with the end 4 byte pointer being equivalent to the one of the given pointer and the begin 4 byte pointer incremented by `int`.
There will be an exception if the resulting pointer will be outside the memory area of the first or if the int is negative.

Accessing the area directly referenced by the pointer as a data type:
The area referenced by the pointer has to be at least [size of data type] bytes wide, otherwise there will be an exception.
The first [size of data type] bytes are returned as the data type (in `get_TYPE` functions), or set to the given value (in `set_TYPE` functions).
- `bool` __get_bool__ `pointer`
- `bool` __set_bool__ `pointer`
- `byte` __get_byte__ `pointer`
- `byte` __set_byte__ `pointer`
- `int` __get_int__ `pointer`
- `int` __set_int__ `pointer`
- `float` __get_float__ `pointer`
- `float` __set_float__ `pointer`
- `pointer` __get_pointer__ `pointer`
- `pointer` __set_pointer__ `pointer`
- `string` __get_string__ `pointer`
- `string` __set_string__ `pointer`

####Strings

- `string` __append__ `string string`
Append the second string to the first and return a pointer to the new string.

Convert them into pointers, work with them and then convert them back.

####IO
- `void` __print__ `string`
Print the string on the standard output.
- `void` __print_err__ `string`
Print the string on the error output.
Note: If the error out isn't provided by the target, it uses the standard out.

- `byte` __read_byte__
Reads a character from standard input.
- `string` __read_string__
Reads a string from standard input.

####Misc
- `int` __exit__ `int`
Exits the program. The integer is an error code if its not zero.
- `void` __assert__ `bool`
Exits and prints some context information (e.g. line and function) if bool is `false`.
- `void` __assert__ `bool string`
Exits and prints the string (with some context information) if bool is `false`.

###Comments
Comments are lines preceeded by a `#` character.
Function comments are comment blocks (several lines preceeded by a `#` character) in front of functions, they can be used to clarify the functionality and usage of the function.
