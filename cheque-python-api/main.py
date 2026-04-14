from typing import Any, Dict, Optional
from fastapi import FastAPI, HTTPException
from pydantic import BaseModel
from scanner import get_cheque

class TicketRequest(BaseModel):
    qr: str

class TicketResponse(BaseModel):
    success: bool
    cheque: Optional[Dict[str, Any]] = None
    error: Optional[str] = None

app = FastAPI()

@app.post("/cheque", response_model=TicketResponse)
def get_ticket(request_body: TicketRequest):
    try:
        ticket_data = get_cheque(request_body.qr)

        return TicketResponse(
            success=True,
            cheque=ticket_data,
            error=None
        )
    except Exception as e:
        raise HTTPException(status_code=500, detail=str(e))