import json
from transformers import AutoTokenizer, AutoModelForCausalLM
import torch
 
def get_weather(location):
    # just fake data for demo
    return {"location": location, "temperature": 22, "condition": "sunny"}
 
def run_demo():
    print("loading model...")
    # use whatever model fits in memory
    model_name = "Qwen/Qwen2.5-7B-Instruct"
    tokenizer = AutoTokenizer.from_pretrained(model_name)
    model = AutoModelForCausalLM.from_pretrained(
        model_name,
        torch_dtype=torch.float16,
        device_map="auto"
    )
    print("model loaded\n")
 
    # observe the tool related instructions in the system prompt
    system_prompt = """You are a helpful assistant with access to this tool:
- get_weather: Get the current weather for a location
 
To use it, respond with JSON: {"tool": "get_weather", "arguments": {"location": "city"}}
Otherwise, respond normally."""
 
    user_query = "What's the weather in Paris?"
 
    messages = [
        {"role": "system", "content": system_prompt},
        {"role": "user", "content": user_query}
    ]
 
    print(">>> PROMPT:")
    print(f"System: {system_prompt}\n")
    print(f"User: {user_query}\n")
 
    # first call - should return tool call
    text = tokenizer.apply_chat_template(messages, tokenize=False, add_generation_prompt=True)
    inputs = tokenizer([text], return_tensors="pt").to(model.device)
    outputs = model.generate(**inputs, max_new_tokens=256, temperature=0.7, do_sample=True)
    generated_ids = outputs[0][len(inputs.input_ids[0]):]
    resp = tokenizer.decode(generated_ids, skip_special_tokens=True).strip()
 
    print(">>> RESPONSE:")
    print(resp)
    print()
 
    try:
        # parse the tool call - imagine this is good parsing
        start = resp.find('{')
        end = resp.rfind('}') + 1
        tool_call = json.loads(resp[start:end])
 
        print(f"calling tool: {tool_call['tool']}")
        print(f"args: {tool_call['arguments']}")
 
        result = get_weather(**tool_call['arguments'])
        print(f"result: {result}\n")
 
        # now ask it to respond with the tool result
        messages.append({"role": "assistant", "content": resp})
        messages.append({"role": "user", "content": f"Tool result: {json.dumps(result)}. Now respond to the user."})
 
        print(">>> PROMPT (with tool result):")
        print(f"Tool result: {json.dumps(result)}")
        print("Now respond to the user.\n")
 
        text = tokenizer.apply_chat_template(messages, tokenize=False, add_generation_prompt=True)
        inputs = tokenizer([text], return_tensors="pt").to(model.device)
        outputs = model.generate(**inputs, max_new_tokens=256, temperature=0.7, do_sample=True)
        generated_ids = outputs[0][len(inputs.input_ids[0]):]
        final_resp = tokenizer.decode(generated_ids, skip_special_tokens=True).strip()
 
        print(">>> FINAL RESPONSE:")
        print(final_resp)
        print()
 
    except Exception as e:
        print(f"error: {e}")
 
if __name__ == "__main__":
    run_demo()
