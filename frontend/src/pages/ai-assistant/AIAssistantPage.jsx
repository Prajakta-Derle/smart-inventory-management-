import { useRef, useState } from 'react';
import { Bot, Send, User, Info } from 'lucide-react';
import aiApi from '../../api/aiApi';

const SUGGESTION_LABEL = 'This is a suggestion — please review before taking action';

// Example prompts for the five supported query types (Req 11.5).
const EXAMPLES = [
  'How many units of each low-stock product should I reorder?',
  'Analyse the demand trend for the last 30 days.',
  'Give me an overall inventory health summary.',
  'Why is product X low on stock?',
  'Summarise the purchase trend this month.',
];

export default function AIAssistantPage() {
  const [messages, setMessages] = useState([]); // { role: 'user'|'ai'|'error', text }
  const [input, setInput] = useState('');
  const [sending, setSending] = useState(false);
  const threadRef = useRef(null);

  const scrollToBottom = () => {
    requestAnimationFrame(() => {
      if (threadRef.current) threadRef.current.scrollTop = threadRef.current.scrollHeight;
    });
  };

  const ask = async (question) => {
    const q = (question ?? input).trim();
    if (!q || sending) return;

    setMessages((m) => [...m, { role: 'user', text: q }]);
    setInput('');
    setSending(true);
    scrollToBottom();

    try {
      const data = await aiApi.query(q);
      const answer = typeof data === 'string' ? data : data?.answer || 'No response.';
      setMessages((m) => [...m, { role: 'ai', text: answer }]);
    } catch (err) {
      // Never show the raw provider error; the backend returns 503 on failure.
      const friendly =
        err.status === 503
          ? 'The AI service is temporarily unavailable. Please try again in a moment.'
          : 'Could not reach the AI assistant. Please check your connection and try again.';
      setMessages((m) => [...m, { role: 'error', text: friendly }]);
    } finally {
      setSending(false);
      scrollToBottom();
    }
  };

  const onSubmit = (e) => {
    e.preventDefault();
    ask();
  };

  return (
    <div className="flex flex-col gap-4">
      <div>
        <h1 className="text-xl font-semibold text-slate-800">AI Assistant</h1>
        <p className="text-sm text-slate-500">
          Ask questions about your inventory. Responses are AI-generated suggestions.
        </p>
      </div>

      {/* Example prompts */}
      {messages.length === 0 ? (
        <div className="flex flex-col gap-2">
          <span className="text-xs font-medium uppercase tracking-wide text-slate-500">Try asking</span>
          <div className="flex flex-wrap gap-2">
            {EXAMPLES.map((ex) => (
              <button
                key={ex}
                type="button"
                onClick={() => ask(ex)}
                className="rounded-full border border-slate-300 bg-white px-3 py-1.5 text-xs text-slate-700 hover:bg-slate-50"
              >
                {ex}
              </button>
            ))}
          </div>
        </div>
      ) : null}

      {/* Conversation thread */}
      <div
        ref={threadRef}
        className="flex max-h-[55vh] min-h-[200px] flex-col gap-3 overflow-y-auto card p-4"
      >
        {messages.length === 0 ? (
          <div className="flex flex-1 items-center justify-center text-sm text-slate-400">
            No messages yet — ask something to get started.
          </div>
        ) : (
          messages.map((m, i) => {
            if (m.role === 'user') {
              return (
                <div key={i} className="flex justify-end">
                  <div className="flex max-w-[80%] items-start gap-2 rounded-lg bg-brand-600 px-3 py-2 text-sm text-white">
                    <span>{m.text}</span>
                    <User size={16} className="mt-0.5 shrink-0 opacity-80" />
                  </div>
                </div>
              );
            }
            if (m.role === 'error') {
              return (
                <div key={i} className="flex justify-start">
                  <div className="max-w-[80%] rounded-lg border border-red-200 bg-red-50 px-3 py-2 text-sm text-red-800">
                    {m.text}
                  </div>
                </div>
              );
            }
            // AI response — always carries the suggestion label.
            return (
              <div key={i} className="flex justify-start">
                <div className="flex max-w-[85%] flex-col gap-2">
                  <div className="flex items-start gap-2 rounded-lg border border-slate-200 bg-slate-50 px-3 py-2 text-sm text-slate-800">
                    <Bot size={16} className="mt-0.5 shrink-0 text-brand-600" />
                    <span className="whitespace-pre-wrap">{m.text}</span>
                  </div>
                  <div className="flex items-center gap-1.5 rounded-md bg-amber-50 px-2 py-1 text-xs font-medium text-amber-800">
                    <Info size={13} /> {SUGGESTION_LABEL}
                  </div>
                </div>
              </div>
            );
          })
        )}
        {sending ? (
          <div className="flex justify-start">
            <div className="rounded-lg border border-slate-200 bg-slate-50 px-3 py-2 text-sm text-slate-500">
              Thinking…
            </div>
          </div>
        ) : null}
      </div>

      {/* Input */}
      <form onSubmit={onSubmit} className="flex items-end gap-2">
        <textarea
          rows={2}
          className="input flex-1"
          placeholder="Ask about reorder quantities, trends, inventory health…"
          value={input}
          onChange={(e) => setInput(e.target.value)}
          onKeyDown={(e) => {
            if (e.key === 'Enter' && !e.shiftKey) {
              e.preventDefault();
              ask();
            }
          }}
        />
        <button
          type="submit"
          disabled={sending || !input.trim()}
          className="btn-primary"
        >
          <Send size={16} /> Send
        </button>
      </form>
    </div>
  );
}
