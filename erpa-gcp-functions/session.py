from contextlib import contextmanager
from os import getenv
from psycopg2 import OperationalError
from psycopg2.pool import ThreadedConnectionPool, SimpleConnectionPool
from sqlalchemy import create_engine
from sqlalchemy.engine.base import Engine
from sqlalchemy.orm import Session, sessionmaker

import models

pg_pool = None


class SessionBroker:
    """Generate a session with the backing db."""
    def __init__(self, database_url: str = None, engine: Engine = None) -> None:
        assert database_url or engine, "Need a database_url or an engine to start a session"
        # global pg_pool
        # if not pg_pool:
        #     pg_pool = SimpleConnectionPool(1, 100, **pg_config)

        self.engine = create_engine(database_url) if engine is None else engine
        self.maybe_initialize_tables(self.engine)
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
