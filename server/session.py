from contextlib import contextmanager
from os import getenv
from psycopg2 import OperationalError
from psycopg2.pool import ThreadedConnectionPool, SimpleConnectionPool
from sqlalchemy import create_engine
from sqlalchemy.engine.base import Engine
from sqlalchemy.orm import Session, sessionmaker
from typing import Optional

import models

db_engines = dict()  # type: Dict[str, Engine]


class SessionBroker:
    """Generate a session with the backing db."""
    def __init__(self, database_url: Optional[str] = None,
                 engine: Optional[Engine] = None) -> None:
        assert database_url or engine, \
            "Need a database_url or an engine to start a session"

        def memoized_engine(database_url: Optional[str]):
            if database_url not in db_engines:
                engine = create_engine(database_url, echo=True)
                engine.pool._use_threadlocal = True
                self.maybe_initialize_tables(engine)
                db_engines[database_url] = engine
            return db_engines[database_url]

        self.engine = memoized_engine(database_url) if engine is None else engine
        self.session_factory = sessionmaker(bind=self.engine)

    @staticmethod
    def maybe_initialize_tables(engine: Engine) -> None:
        models.Base.metadata.create_all(engine)

    @contextmanager
    def get_session(self) -> Session:
        session = self.session_factory()
        yield session
        session.flush()  # Flush entities before expunging to converge instances
        session.expunge_all()
        if session.is_active:
            session.commit()
        session.close()
