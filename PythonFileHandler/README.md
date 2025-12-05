# Python file handler

## 1. Overview
This project was built using Generative AI tools. The Python code manipulates data in a text file that is a log file for the WordPress XMLRPC interface.

## 2. AWS features used
- [Amazon Q](https://aws.amazon.com/q/) Chat, [Free Tier](https://aws.amazon.com/q/developer/pricing/) version

## 3. Generative AI
The syntax of the script was generated and adjusted in IDEA IDE, using [Amazon Q Chat](https://plugins.jetbrains.com/plugin/24267-amazon-q). 
I had few ideas about Python, but code generation in unknown languages is one of the rapidly growing use-cases of Generative AI. 
From an initial prompt that described the program in natural language, Amazon Q generated a first version, that I chaged according to business needs, with subsequent requests done in same chat session that was initiated. This first version was in a scripting style. 
When I requested an improved version of the script, Amazon Q generated a version in OOP style, that I adjusted to look like the presented one.

## 3. Python features used
- The log feature of Python is used to log towards two channels: file, console
- Environment variables are stored in config file and loaded using Python feature
- Aspect-Oriented Programming decorator for logging
- Email notification with the result of the script run
- Regular expressions to detect sections of the input file
- Python Object Oriented Programming
- Python OOP inheritance with annotations: abstractmethod and classmethod
- Python OOP polymorphism with member and function overriding 
- Python OOP staticmethods
- script command line arguments
