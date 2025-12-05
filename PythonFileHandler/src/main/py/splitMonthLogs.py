#!/usr/bin/env python3
import os
import logging
from logging.handlers import RotatingFileHandler
from datetime import datetime, timedelta
from typing import Optional, Tuple
from functools import wraps
from abc import ABC, abstractmethod
import smtplib
from email.message import EmailMessage
from dotenv import load_dotenv
import re
from dateutil.relativedelta import relativedelta
import argparse
from pathlib import Path

# Load environment variables from config.env file
project_dir_name = "PythonFileHandler"
current_dir = Path(__file__)
project_dir = [p for p in current_dir.parents if p.parts[-1] == project_dir_name][0]
dotenv_path = os.path.join(project_dir, 'resources', 'config.env')
print(f"Loading environment variables from {dotenv_path}")
load_dotenv(dotenv_path, verbose=True)

# set global variables
BASE_DIR_NAME = os.getenv('BASE_DIR_NAME')
if not BASE_DIR_NAME:
    raise ValueError("BASE_DIR_NAME environment variable is not set")
else:
    print(f"BASE_DIR_NAME: {BASE_DIR_NAME}")

environments = ["DEV", "TEST", "PROD"]
environment = environments[1]

input_file = os.path.join(BASE_DIR_NAME, os.getenv('INPUT_FILE_NAME'))


# Configure logging with 2 handlers: file, console
def setup_logging() -> logging.Logger:
    loggr = logging.getLogger('XMLRPCsplitter')
    loggr.setLevel(logging.DEBUG)

    handler = RotatingFileHandler(
        BASE_DIR_NAME + 'xmlrpc-process.log',
        maxBytes=1024 * 1024,  # 1MB
        backupCount=5
    )
    formatter = logging.Formatter(fmt='[%(asctime)s:%(levelname)10s:%(funcName)20s():%(lineno)s] %(message)s ',
                                  datefmt='%m/%d/%Y %I:%M:%S %p')
    handler.setFormatter(formatter)
    handler.setLevel(logging.INFO)
    loggr.addHandler(handler)

    # create console handler
    ch = logging.StreamHandler()
    ch.setLevel(logging.DEBUG)
    ch.setFormatter(formatter)
    loggr.addHandler(ch)

    return loggr


logger = setup_logging()

logger.info(f"environment: {environment}")


# Aspect-Oriented Programming decorator for logging
def log_operation(func):
    @wraps(func)
    def wrapper(*args, **kwargs):
        logger.info(f"START function: "
                    f"{func.__module__}."
                    f"{func.__name__} "
                    f"{args}")
        try:
            result = func(*args, **kwargs)
            logger.info("END function: '%s' with result: %s" % (func.__name__, result))
            return result
        except Exception as e:
            logger.error(f"Error in function '{func.__name__}': {str(e)}")
            raise

    return wrapper


# handle email notification with the result of the script
class EmailNotifier:
    def __init__(self):
        self.smtp_server = os.getenv('SMTP_SERVER')
        self.smtp_port = int(os.getenv('SMTP_PORT'))
        self.smtp_user = os.getenv('SMTP_USER')
        self.smtp_password = os.getenv('SMTP_PASSWORD')
        self.email_recipient = os.getenv('EMAIL_RECIPIENT')
        self.sender_email = "site_script_xmlrpc@mihaiadam.com"

        if not all([self.smtp_server, self.smtp_port, self.smtp_user, self.smtp_password, self.email_recipient,
                    self.sender_email]):
            raise ValueError("Missing required environment variables")

    @log_operation
    def send_email(self, subject: str, message: str) -> bool:
        if environment == "DEV":
            logger.info("Email sending is disabled in development environment")
            return True
        try:
            msg = EmailMessage()
            msg.set_content(message)
            msg['Subject'] = subject
            msg['From'] = self.sender_email
            msg['To'] = self.email_recipient

            with smtplib.SMTP(self.smtp_server, self.smtp_port) as server:
                server.starttls()
                server.login(self.smtp_user, self.smtp_password)
                server.send_message(msg)
            return True
        except Exception as e:
            logger.error(f"Failed to send email: {str(e)}")
            return False


# split file according to the script input option
class FileProcessor(ABC):

    datetime_pattern_grouped = re.compile(r'^<datetime>(.+?)</datetime>$')
    processor_name = "FileProcessor"

    def __init__(self):
        self.input_number_of_records = 0
        self.output_number_of_records = 0

    @classmethod
    # Automatically receives the class as first parameter
    # Aware of inheritance (works with subclasses)
    def get_processor_name(cls):
        return cls.processor_name

    @staticmethod
    # Receives no automatic parameters
    # Not aware of inheritance
    def get_previous_month():
        current_date = datetime.now()
        one_month_ago = current_date - relativedelta(months=1)
        return one_month_ago.strftime("%Y.%m")

    @staticmethod
    def is_previous_month(self, test_date):
        current_date = datetime.now()
        one_month_ago = current_date - relativedelta(months=1)

        return (test_date.year == one_month_ago.year and
                test_date.month == one_month_ago.month)

    @staticmethod
    def is_previous_second_month(self, test_date):
        current_date = datetime.now()
        two_month_ago = current_date - relativedelta(months=2)

        return (test_date.year <= two_month_ago.year and
                test_date.month <= two_month_ago.month)

    @staticmethod
    def valid_datetime_line(self, line):

        try:
            # Extract the datetime string (without tags)
            datetime_str = FileProcessor.datetime_pattern_grouped.search(line).group(1)
            # Try to parse it
            datetime.strptime(datetime_str, '%Y-%m-%d %H:%M:%S %z')
            return True
        except (ValueError, AttributeError):
            return False

    @log_operation
    def process(self) -> bool:

        try:
            if os.path.exists(self.get_output_filename()):
                logger.error("File already exists: %s" % self.get_output_filename)
                raise FileExistsError("File already exists: %s" % self.get_output_filename)

            # Initialize variables
            # collecting should be True only inside open/close tags of one record
            collecting = False
            collected_lines = []
            date_string = "2025-10-01 10:24:04 +0000"

            # Read input file and process lines into output file
            output_file = self.get_output_filename()
            infile = open(input_file, 'r', encoding='utf-8')
            outfile = open(output_file, 'a', encoding='utf-8')

            for line in infile:
                collected_lines.append(line)
                # Check for start tag
                if '<xmlPost>' in line:
                    if collecting:
                        raise RuntimeError("Unexpected nested <xmlPost> tag found")
                    if not '<xmlPost>' == line.strip():
                        raise RuntimeError("Strange nested <xmlPost> tag found")
                    collecting = True

                # Check for datetime
                if '<datetime>' in line:
                    if not collecting:
                        raise RuntimeError("Unexpected nested <datetime> tag found")
                    if not self.valid_datetime_line(self, line):
                        raise RuntimeError("Strange nested <datetime> tag found")

                    # extract text between "<datetime>" and "</datetime>" tags
                    date_string = self.datetime_pattern_grouped.search(line).group(1)
                    logger.debug(f"date_string from record: {date_string}")

                # Check for end tag
                if '</xmlPost>' in line:

                    if not collecting:
                        raise RuntimeError("Unexpected nested </xmlPost> tag found")
                    if not '</xmlPost>' == line.strip():
                        raise RuntimeError("Strange nested </xmlPost> tag found")

                    parsed_date = datetime.strptime(date_string, "%Y-%m-%d %H:%M:%S %z")

                    # Write collected lines to output file
                    if self.allow_write(parsed_date):
                        self.output_number_of_records += 1
                        outfile.writelines(collected_lines)

                    collected_lines = []
                    collecting = False
                    self.input_number_of_records += 1

            outfile.close()
            infile.close()

            return True

        except Exception as e:
            logger.error(f"An error occurred: {str(e)}")
            return False

    # filter specific rows for output file, filter depends on run-option
    @abstractmethod
    def allow_write(self, parsed_date: datetime) -> bool:
        pass

    # get output file name that depends on run-option
    @abstractmethod
    def get_output_filename(self) -> str:
        pass


# concrete implementation of class FileProcessor for option 'KeepPreviousMonth'
class KeepPreviousMonthProcessor(FileProcessor):

    # overridden static member
    processor_name = "KeepPreviousMonthProcessor"

    @classmethod
    def get_processor_name(cls):  # Override the method
        return cls.processor_name

    def allow_write(self, parsed_date: datetime) -> bool:
        return self.is_previous_month(self, parsed_date)

    @log_operation
    def get_output_filename(self) -> str:
        return BASE_DIR_NAME + ("xmlrpc-debug-%s.log" % FileProcessor.get_previous_month())


# concrete implementation of class FileProcessor for option 'deletePastSecondMonth'
class DeletePastSecondMonthProcessor(FileProcessor):

    # overridden static member
    processor_name = "DeletePastSecondMonthProcessor"

    @classmethod
    def get_processor_name(cls):  # Override the method
        return cls.processor_name

    def allow_write(self, parsed_date: datetime) -> bool:
        return not self.is_previous_second_month(self, parsed_date)

    @log_operation
    def get_output_filename(self) -> str:
        return BASE_DIR_NAME + "xmlrpc-debug.log"


def main() -> int:
    logger.info("START XMLRPC processor")
    # Set up argument parser
    parser = argparse.ArgumentParser(description='Process XML log files')
    parser.add_argument('--option',
                        choices=['keepPreviousMonth', 'deletePastSecondMonth'],
                        required=True,
                        help='Specify the processing option')
    # Parse arguments
    args = parser.parse_args()
    option = args.option
    logger.info("Run option: %s" % option)

    notifier = EmailNotifier()

    try:
        # Get configuration from environment variables
        if not all([option]):
            raise ValueError("Missing required environment variables")

        # Define variables
        option = args.option

        # remind what we print
        logger.debug("Debugging")
        logger.info("Informational message")
        logger.warning("Warning message")
        logger.error("Error message")
        logger.critical("Critical message")

        processor: FileProcessor = None

        # in both options the init xml log file is copied to a new file
        match option:
            case "keepPreviousMonth":
                processor = KeepPreviousMonthProcessor()

            case "deletePastSecondMonth":
                processor = DeletePastSecondMonthProcessor()

            case _:  # This is the default case
                logger.error("Unexpected option")
                raise RuntimeError("Unexpected option")

        # Process the file
        success = False
        if processor:
            logger.info(f"File processing started by processor: {processor.__class__.__name__}")
            success = processor.process()

        if success:
            logger.info(f"File processing completed successfully "
                        f"by processor: {processor.get_processor_name()}, "
                        f"with input number of records: {processor.input_number_of_records},  "
                        f"and with output number of records: {processor.output_number_of_records}.")

            # Send notification email
            notifier.send_email(subject='File Processing Completed',
                                message=f"""
                File processing completed successfully 
                by processor: {processor.get_processor_name()},
                with input number of records: {processor.input_number_of_records},
                and with output number of records: {processor.output_number_of_records}.
                
                Processing agent,
                Python script
                """)
            return 0
        else:
            logger.info("File processing failed!")
            notifier.send_email(subject='File Processing Failed',
                                message=f"""
                File processing failed !
                
                Processing agent,
                Python script
                """)
            return 1

    except Exception as e:
        logger.error(f"An error occurred: {str(e)}")
        notifier.send_email(subject='File Processing Error',
                            message=f"""
                File processing failed with error: {str(e)} !
                
                Processing agent,
                Python script
                """)
        return 1

    finally:
        logger.info("END XMLRPC processor")
        return 0


if __name__ == "__main__":
    exit(main())
